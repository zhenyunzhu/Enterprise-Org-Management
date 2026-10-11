package com.itheima.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 *
 * <p>标记在 Controller 的增删改方法上，由 {@code OperationLogAspect} 拦截并
 * 将操作人、操作时间、类名、方法名、入参、返回值与耗时写入 {@code operate_log} 表。</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
}
