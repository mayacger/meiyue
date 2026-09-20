package com.meiyuemall.common.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * I7：标记需要写入审计日志的写操作方法。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    /** 动作码，如 PRODUCT_CREATE */
    String action();

    /** 资源类型，如 Product */
    String resourceType() default "";
}
