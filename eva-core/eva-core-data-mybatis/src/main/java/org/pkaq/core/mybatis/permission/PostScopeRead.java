package org.pkaq.core.mybatis.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 明确登记的纯读取控制器入口，不允许类级别或自动按GET登记。
 * @author PKAQ
 * @date 2026-11-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostScopeRead {
}
