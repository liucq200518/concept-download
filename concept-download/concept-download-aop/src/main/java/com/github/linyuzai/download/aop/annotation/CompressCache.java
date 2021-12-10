package com.github.linyuzai.download.aop.annotation;

import com.github.linyuzai.download.core.compress.Compressible;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CompressCache {

    boolean enabled() default true;

    String group() default "";

    String name() default "";

    boolean delete() default false;
}
