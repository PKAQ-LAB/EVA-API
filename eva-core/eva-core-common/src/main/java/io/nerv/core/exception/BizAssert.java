package io.nerv.core.exception;

import io.nerv.core.enums.BizCode;

import java.text.MessageFormat;
import java.util.Collection;

/**
 * @author PKAQ
 */
public interface BizAssert extends BizCode {
    /**
     * 断言异常
     *
     * @param args
     * @return
     */
    default BizException newException(Object... args) {
        String msg = MessageFormat.format(this.getMsg(), null == args ? "" : args);
        throw new BizException(msg);
    }

    /**
     * 抛出指定异常
     *
     * @param t
     * @return
     */
    default BizException newException(Throwable t) {
        return new BizException(this, t);
    }

    /**
     * 抛出指定异常
     *
     * @param t
     * @param args
     * @return
     */
    default BizException newException(Throwable t, Object... args) {
        return new BizException(this, t, args);
    }

    /**
     * <p>断言对象<code>obj</code>非空。如果对象<code>obj</code>为空，则抛出异常
     *
     * @param obj 待判断对象
     */
    default void assertNotBlank(Object obj) {
        if (obj instanceof Collection && (null == obj || ((Collection<?>) obj).size() == 0)) {
            throw newException(obj);
        }

        if (obj instanceof CharSequence && (null == obj || ((CharSequence) obj).length() == 0)) {
            throw newException(obj);
        }
    }

    /**
     * <p>断言对象<code>obj</code>非空。如果对象<code>obj</code>为空，则抛出异常
     *
     * @param obj 待判断对象
     */
    default void assertNotNull(Object obj) {
        if (obj == null) {
            throw newException();
        }
    }

    /**
     * <p>断言对象<code>obj</code>非空。如果对象<code>obj</code>为空，则抛出异常
     * <p>异常信息<code>message</code>支持传递参数方式，避免在判断之前进行字符串拼接操作
     *
     * @param obj  待判断对象
     * @param args message占位符对应的参数列表
     */
    default void assertNotNull(Object obj, Object... args) {
        if (obj == null) {
            throw newException(args);
        }
    }

    /**
     * @param obj
     * @param t
     * @param args
     */
    default void assertNotNullException(Object obj, Throwable t, Object... args) {
        if (obj == null) {
            throw newException(t, args);
        }
    }
}