package org.pkaq.core.mvc.convert;

import jakarta.annotation.PostConstruct;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.core.mvc.entity.Entity;
import org.pkaq.core.mvc.vo.Vo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动扫描实现类的所有实现方法
 *
 * @author PKAQ
 */
public abstract class Convert {
    // 缓存 Bo 类型到 fromBo 方法的映射
    private final Map<Class<?>, Method> convertMap = new HashMap<>();

    @PostConstruct
    private void init() {
        // 扫描当前子类所有 public 方法
        for (Method method : this.getClass().getMethods()) {
            if (method.getParameterCount() != 1) continue;

            Class<?> paramType = method.getParameterTypes()[0];
            // ✅ 只注册 Bo、Vo、Entity 类型的实现类/ 跳过非核心模型类型
            if (!Bo.class.isAssignableFrom(paramType)
                    && !Entity.class.isAssignableFrom(paramType)
                    && !Vo.class.isAssignableFrom(paramType)) {
                continue;
            }

            convertMap.put(paramType, method);
        }
    }

    @SuppressWarnings("unchecked")
    public <R> R fromBo(Bo bo) {
        return this.convert(bo);
    }

    @SuppressWarnings("unchecked")
    public <R> R toVo(Entity entity) {
        return this.convert(entity);
    }

    private <R> R convert(Object param) {
        if (param == null) return null;

        Method method = convertMap.get(param.getClass());
        if (method == null) {
            // 可选：支持父类或接口多态匹配
            method = convertMap.entrySet().stream()
                    .filter(e -> e.getKey().isAssignableFrom(param.getClass()))
                    .map(Map.Entry::getValue)
                    .findFirst().orElse(null);
        }

        if (method == null) throw new BizException(CommonCodes.SERVER_ERROR_CONVERT);

        try {
            return (R) method.invoke(this, param);
        } catch (Exception e) {
            throw new BizException(CommonCodes.SERVER_ERROR_CONVERT);
        }
    }

}