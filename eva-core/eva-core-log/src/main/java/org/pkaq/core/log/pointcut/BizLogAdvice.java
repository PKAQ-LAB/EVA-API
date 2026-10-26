package org.pkaq.core.log.pointcut;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.events.BizLogEvent;
import org.pkaq.core.log.events.BusinessLogEventHandler;
import org.pkaq.core.log.util.LogSanitizer;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.ArrayUtils;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.ReflectUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 异步记录日志
 *
 * @author PKAQ
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.bizlog", name = "enabled", havingValue = "true")
public class BizLogAdvice {
    private static final int MAX_PARAMS_LENGTH = 16_000;
    private static final int MAX_RESPONSE_LENGTH = 32_000;
    private static final int MAX_DESCRIPTION_LENGTH = 4_000;

    private final ApplicationEventPublisher eventPublisher;
    private final I18NHelper i18NHelper;
    private final String formatArg = "param:";
    private final String formatResult = "this";

    @Pointcut("@annotation(org.pkaq.core.log.annotation.BizLog)")
    private void bizLog() {
    }

    @Around("bizLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long beginTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog bizlog = signature.getMethod().getAnnotation(BizLog.class);
        // 是否是事务方法
        boolean isTransactional = Objects.nonNull(signature.getMethod().getAnnotation(Transactional.class));

        // 创建日志实体类
        Schema schema = joinPoint.getTarget().getClass().getAnnotation(Schema.class);
        var description = bizlog.description();
        // 如果是以{}包裹的字符串就使用i18n获取国际化文案

        if (StrUtils.isNotBlank(description) && description.startsWith("{") && description.endsWith("}")) {
            description = i18NHelper.getMessage(description.replace("{", "").replace("}", ""), description);
        }

        if (Objects.nonNull(schema)) {
            description = i18NHelper.getMessage("sys.operate.description", "操作描述：") + description;
        }

        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        var args = LogSanitizer.sanitize(JsonUtil.toJson(joinPoint.getArgs()), MAX_PARAMS_LENGTH);
        // 根据方法入参设置操作描述的格式化参数 并返回需要的响应参数名
        var descriptionArgs = bizlog.args();
        if (StrUtils.isNotBlank(bizlog.bizId())) {
            descriptionArgs = ArrayUtils.add(descriptionArgs, bizlog.bizId());
        }
        var formatArgs = new Object[descriptionArgs.length];
        var rMap = processArgs(joinPoint.getArgs(), descriptionArgs, formatArgs);
        // 根据参数的某个属性是否为空来判断是新增还是修改
        var operatorType = bizlog.operateType();
        if (BizLogCodes.EDIT.equals(operatorType)) {
            operatorType = processOperatorType(bizlog.distinguishParam(), joinPoint.getArgs());
        }
        BizLogEntity bizLogEntity = new BizLogEntity();
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        long tenantId = currentUser == null ? 0L : currentUser.getTenantId();
        long userId = currentUser == null ? 0L : currentUser.getUserId();
        bizLogEntity.setTenantId(tenantId)
                .setUserId(userId)
                .setOperator(currentUser == null ? "anonymous" : currentUser.getName())
                .setOperateDatetime(DateUtils.now())
                .setOperateType(operatorType.getCode())
                .setClassName(className)
                .setMethod(methodName)
                .setParams(args)
                .setMCode(ThreadUserHelper.getMcode())
                .setDevice(ThreadUserHelper.getDevice())
                .setVersion(currentUser == null ? "" : currentUser.getVersion())
                .setSuccess(Boolean.FALSE);

        Object result;
        try {
            result = joinPoint.proceed();
            bizLogEntity.setResponse(LogSanitizer.sanitize(JsonUtil.toJson(result), MAX_RESPONSE_LENGTH));
            bizLogEntity.setSuccess(Boolean.TRUE);
            // 根据响应设置操作描述的格式化参数
            processResult(result, rMap, formatArgs);
        } catch (Exception e) {
            // 无事务时操作失败不会走AFTER_ROLLBACK监听器 所以手动设置操作失败的记录
            if (!isTransactional) {
                description = "%s%s".formatted(BusinessLogEventHandler.FAILURE_PREFIX, description);
            }
            throw e;
        } finally {
            // 操作类型为新增 id在新增之后才会回显到入参中 所以需要重新处理一下
            if (BizLogCodes.CREATE.equals(operatorType)) {
                processArgs(joinPoint.getArgs(), descriptionArgs, formatArgs);
            }
            Object[] safeFormatArgs = new Object[formatArgs.length];
            for (int index = 0; index < formatArgs.length; index++) {
                safeFormatArgs[index] = safeDescriptionArgument(descriptionArgs[index], formatArgs[index]);
            }
            if (StrUtils.isNotBlank(bizlog.bizId()) && safeFormatArgs[safeFormatArgs.length - 1] != null) {
                bizLogEntity.setBId(String.valueOf(safeFormatArgs[safeFormatArgs.length - 1]));
            }
            bizLogEntity.setDescription(LogSanitizer.sanitize(
                    MessageFormat.format(description, safeFormatArgs), MAX_DESCRIPTION_LENGTH));
            bizLogEntity.setSpendTime(String.valueOf(System.currentTimeMillis() - beginTime));
            // 触发事件 使用事务监听器异步保存操作记录
            eventPublisher.publishEvent(new BizLogEvent(bizLogEntity));
        }
        return result;
    }

    private Object safeDescriptionArgument(String source, Object value) {
        if (LogSanitizer.isSecretField(source)) {
            return "******";
        }
        if (value == null || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof CharSequence) {
            String sanitized = LogSanitizer.sanitize(value.toString(), MAX_DESCRIPTION_LENGTH);
            // 结构化参数不重新嵌入描述，避免整个模板退化为无法完整解析的JSON文本。
            return sanitized.startsWith("{") || sanitized.startsWith("[") || sanitized.startsWith("\"")
                    ? "******" : sanitized;
        }
        if (value instanceof Collection<?> values && values.stream().allMatch(Number.class::isInstance)) {
            return JsonUtil.toJson(values);
        }
        // 整个业务对象仅投影数字标识，避免调用未知对象的toString或携带凭据字段。
        Map<String, Object> identifiers = new LinkedHashMap<>();
        for (String field : List.of("id", "userId", "roleId", "tenantId", "mainId", "param")) {
            Object identifier = value instanceof Map<?, ?> values
                    ? values.get(field) : ReflectUtils.getFieldValue(value, field);
            if (identifier instanceof Number || identifier instanceof Collection<?> values
                    && values.stream().allMatch(Number.class::isInstance)) {
                identifiers.put(field, identifier);
            }
        }
        return value.getClass().getSimpleName() + identifiers.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    /**
     * 根据方法入参设置操作描述的格式化参数 并返回需要的响应参数名
     *
     * @param args       方法的实参
     * @param bizArgs    BizLog注解的args属性
     * @param formatArgs format的入参值
     *                   MessageFormat.format(bizCode.getMsg(), args)
     * @return 返回值的属性和它们在formatArgs的下标
     */
    private Map<String, List<Integer>> processArgs(Object[] args, String[] bizArgs, Object[] formatArgs) {
        if (bizArgs.length == 0) {
            return Collections.emptyMap();
        }

        // format需要的方法入参/返回值的属性名和顺序
        Map<String, List<Integer>> pMap = HashMap.newHashMap(bizArgs.length);
        Map<String, List<Integer>> rMap = HashMap.newHashMap(bizArgs.length);
        // 方法的第几个参数和所需的属性
        Map<Integer, List<String>> pArgsMap = HashMap.newHashMap(bizArgs.length);
        int i = 0;
        try {
            for (String param : bizArgs) {
                // 操作描述的format值来自于方法入参
                if (param.startsWith(formatArg)) {
                    var pValue = param.substring(formatArg.length());
                    pMap.computeIfAbsent(pValue, k -> new ArrayList<>()).add(i++);

                    // 复杂对象 0.xxx  需要第1个参数的xxx属性
                    if (pValue.length() > 1) {
                        // 第几个入参和所需的属性名
                        var key = Integer.valueOf(pValue.substring(0, 1));
                        pArgsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(pValue.substring(2));
                        continue;
                    }
                    // 简单对象 0 需要第1个参数的值
                    pArgsMap.put(Integer.valueOf(pValue), null);
                    continue;
                }
                // 操作描述的format值来自于方法返回值
                rMap.computeIfAbsent(param, k -> new ArrayList<>()).add(i++);
            }

            // 根据所需入参 得到实际的值 并放在formatArgs中
            if (pMap.isEmpty()) {
                return rMap;
            }
            // k表示 方法的入参下标  v表示这个下标对象的属性
            pArgsMap.forEach((k, v) -> {
                String key = k.toString();
                Object val = args[k];
                if (v != null) {
                    for (String n : v) {
                        // 复杂对象
                        key = "%s.%s".formatted(k, n);
                        var obj = args[k];
                        try {
                            val = getFieldValue(obj, n);
                        } catch (Exception e) {
                            val = "";
                            log.error("根据方法实参构造格式化参数异常");
                        }
                        Object fieldValue = val;
                        pMap.get(key).forEach(index -> formatArgs[index] = fieldValue);
                    }
                    return;
                }
                Object finalVal = val;
                pMap.get(key).forEach(index -> formatArgs[index] = finalVal);
            });
        } catch (Exception e) {
            log.error("构造业务日志入参摘要失败");
        }
        return rMap;
    }

    /**
     * 根据返回对象构造格式化参数
     *
     * @param formatArgs 格式化参数数组
     * @param rMap       format需要的方法返回值的属性名和顺序
     * @param result     方法的返回对象
     */
    private void processResult(Object result, Map<String, List<Integer>> rMap, Object[] formatArgs) {
        if (CollUtils.isEmpty(rMap)) {
            return;
        }
        try {
            rMap.forEach((k, v) -> {
                Object value = result;
                if (!formatResult.equalsIgnoreCase(k)) {
                    try {
                        value = getFieldValue(result, k);
                    } catch (Exception e) {
                        value = "";
                        log.error("根据返回对象构造格式化参数异常");
                    }
                }
                Object finalValue = value;
                v.forEach(index -> formatArgs[index] = finalValue);
            });
        } catch (Exception e) {
            log.error("构造业务日志返回值摘要失败");
        }
    }


    /**
     * service方法根据参数判断是新增还是修改时  操作类型也根据同样的参数来判断
     *
     * @param distinguishParam 区分新增或修改的参数 0.id表示取第一个入参的id属性 id有值为修改 无则新增
     * @param args             方法入参
     */
    private BizLogCodes processOperatorType(String distinguishParam, Object[] args) {
        try {
            var index = Integer.parseInt(distinguishParam.substring(0, 1));
            var param = distinguishParam.substring(2);
            var value = getFieldValue(args[index], param);
            if (!Objects.isNull(value)) {
                return BizLogCodes.UPDATE;
            }
        } catch (Exception ignored) {
            // 无法判断新增/修改 默认新增
        }
        return BizLogCodes.CREATE;
    }

    /**
     * 得到对象的属性值 属性可能继承于超类
     *
     * @param object 对象
     * @param param  属性名
     * @return 属性值
     */
    private Object getFieldValue(Object object, String param) {
        return ReflectUtils.getFieldValue(object, param);
    }
}
