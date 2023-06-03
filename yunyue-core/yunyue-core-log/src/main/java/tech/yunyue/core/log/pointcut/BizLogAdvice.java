package tech.yunyue.core.log.pointcut;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import tech.yunyue.core.log.annotation.BizLog;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.BizLogEnum;
import tech.yunyue.core.log.condition.BizlogSupporterCondition;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;

/**
 * 记录业务日志
 *
 * @author PKAQ
 */
@Slf4j
@Aspect
@Component
@Conditional(BizlogSupporterCondition.class)
@RequiredArgsConstructor
public class BizLogAdvice {
    private final ApplicationEventPublisher eventPublisher;
    private String formatArg = "param:";
    private String formatResult = "this";

    @Pointcut("@annotation(tech.yunyue.core.log.annotation.BizLog)")
    private void bizLog() {
    }

    @Around("bizLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog bizlog = signature.getMethod().getAnnotation(BizLog.class);
        boolean isTransactional = Objects.nonNull(signature.getMethod().getAnnotation(Transactional.class)); //是否是事务方法

        //创建日志实体类
        Schema schema = joinPoint.getTarget().getClass().getAnnotation(Schema.class);
        var description = bizlog.description();
        if (Objects.nonNull(schema)) {
            description = new StringBuilder().append("操作模块：").append(schema.description())
                                            .append("。操作描述：").append(description).toString();
        }
        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        var args = JsonUtil.toJson(joinPoint.getArgs());
        //根据方法入参设置操作描述的格式化参数 并返回需要的响应参数名
        var formatArgs = new Object[bizlog.args().length];
        var rMap = processArgs(joinPoint.getArgs(),bizlog.args(),formatArgs);
        //根据参数的某个属性是否为空来判断是新增还是修改
        var operatorType = bizlog.operateType();
        if (BizLogEnum.CREATE_UPDATE.equals(operatorType)) {
            operatorType = processOperatorType(bizlog.distinguishParam(),joinPoint.getArgs());
        }
        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setOperator(ThreadUserHelper.getUserName())
                    .setOperateDatetime(DateUtil.now())
                    .setOperateType(operatorType.getCode())
                    .setClassName(className)
                    .setMethod(methodName)
                    .setParams(args);

        Object result;
        try {
            result = joinPoint.proceed();
            var response = JsonUtil.toJson(result);
            //查询接口不需要保存响应
            if(operatorType.equals(BizLogEnum.QUERY)) bizLogEntity.setResponse(response);
            //根据响应设置操作描述的格式化参数
            processResult(result, rMap, formatArgs);
        } catch (Exception e){
            //无事务时操作失败不会走AFTER_ROLLBACK监听器 所以手动设置操作失败的记录
            if(!isTransactional) description = "【操作失败】" + description;
            throw e;
        } finally {
            //操作类型为新增 id在新增之后才会回显到入参中 所以需要重新处理一下
            if (BizLogEnum.CREATE.equals(operatorType)) {
                processArgs(joinPoint.getArgs(),bizlog.args(),formatArgs);
            }
            bizLogEntity.setDescription(MessageFormat.format(description, formatArgs));
            //触发事件 使用事务监听器异步保存操作记录
            Map<String,Object> map = new HashMap<>(1);
            map.put(isTransactional ? LogConstant.TRANSACTIONAL_LOG : LogConstant.EVENT_LOG,bizLogEntity);
            BizLogEvent bizLogEvent = new BizLogEvent(map);
            eventPublisher.publishEvent(bizLogEvent);
        }
        return result;
    }

    /**
     *根据方法入参设置操作描述的格式化参数 并返回需要的响应参数名
     * @param args 方法的实参
     * @param bizArgs BizLog注解的args属性
     * @param formatArgs format的入参值
     *             MessageFormat.format(bizCode.getMsg(), args)
     * @return 返回值的属性和它们在formatArgs的下标
     */
    private Map<String,Integer> processArgs(Object args[],String[] bizArgs, Object[] formatArgs) {
        if (bizArgs.length == 0) return null;

        //format需要的方法入参/返回值的属性名和顺序
        Map<String,Integer> pMap=new HashMap(bizArgs.length);
        Map<String,Integer> rMap=new HashMap<>(bizArgs.length);
        //方法的第几个参数和所需的属性
        Map<Integer,List<String>> pArgsMap=new HashMap<>(bizArgs.length);
        int i = 0;
        try{
            for (String param : bizArgs) {
                //操作描述的format值来自于方法入参
                if (param.startsWith(formatArg)) {
                    var pValue = param.substring(formatArg.length());
                    pMap.put(pValue, i++);

                    //复杂对象 0.xxx  需要第1个参数的xxx属性
                    if(pValue.length()>1){
                        //第几个入参和所需的属性名
                        var key = Integer.valueOf(pValue.substring(0,1));
                        List<String> names = pArgsMap.getOrDefault(key,new ArrayList());
                        names.add(pValue.substring(2));
                        pArgsMap.put(key, names);
                        continue;
                    }
                    //简单对象 0 需要第1个参数的值
                    pArgsMap.put(Integer.valueOf(pValue),null);
                    continue;
                }
                //操作描述的format值来自于方法返回值
                rMap.put(param,i++);
            }

            //根据所需入参 得到实际的值 并放在formatArgs中
            if (pMap.size() == 0) return rMap;
            //k表示 方法的入参下标  v表示这个下标对象的属性
            pArgsMap.forEach((k, v) -> {
                if (v != null) {
                    for(String n:v){
                        //复杂对象
                        int index = pMap.get(k+"."+n);
                        var obj = args[k];
                        try {
                            formatArgs[index] = getFieldValue(obj,n);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            formatArgs[index] = "";
                            log.error("根据方法实参构造格式化参数异常:" + e.getMessage());
                        }
                    }
                } else {
                    //简单对象
                    formatArgs[pMap.get(k.toString())] = args[k];
                }
            });
        } catch (Exception e){
            log.error("记录日志异常:" + e.getMessage());
            e.printStackTrace();
        }
        return rMap;
    }

    /**
     * 根据返回对象构造格式化参数
     * @param formatArgs 格式化参数数组
     * @param rMap  format需要的方法返回值的属性名和顺序
     * @param result 方法的返回对象
     */
    private void processResult(Object result, Map<String, Integer> rMap, Object[] formatArgs) {
        if(CollectionUtil.isEmpty(rMap)) return;
        try {
            rMap.forEach((k, v) -> {
                Object value = result;
                if (!formatResult.equalsIgnoreCase(k)) {
                    try {
                        value = getFieldValue(result,k);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        value = "";
                        log.error("根据返回对象构造格式化参数异常:" + e.getMessage());
                    }
                }
                formatArgs[v] = value;
            });
        } catch (Exception e){
            log.error("记录日志异常:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * service方法根据参数判断是新增还是修改时  操作类型也根据同样的参数来判断
     * @param distinguishParam 区分新增或修改的参数 0.id表示取第一个入参的id属性 id有值为修改 无则新增
     * @param args 方法入参
     */
    private BizLogEnum processOperatorType(String distinguishParam, Object[] args) {
        try {
            var index = Integer.parseInt(distinguishParam.substring(0, 1));
            var param = distinguishParam.substring(2);
            var value =  getFieldValue(args[index], param);
            if(!Objects.isNull(value)){
                return BizLogEnum.UPDATE;
            }
        }catch (Exception ignored){}
        return BizLogEnum.CREATE;
    }

    /**
     * 得到对象的属性值 属性可能继承于超类
     * @param object 对象
     * @param param 属性名
     * @return 属性值
     */
    private Object getFieldValue(Object object, String param) throws NoSuchFieldException, IllegalAccessException  {
        Class objClass = object.getClass();
        Class superClass = objClass.getSuperclass();
        //当前类属性在前 超类属性在后
        Field [] fields = objClass.getDeclaredFields();
        while (superClass != null) {
            fields = ArrayUtil.addAll(fields, superClass.getDeclaredFields());
            superClass = superClass.getSuperclass();
        }
        Field field = Arrays.stream(fields).filter(f -> f.getName().equals(param)).findFirst().get();
        field.setAccessible(true);
        return field.get(object);
    }
}

