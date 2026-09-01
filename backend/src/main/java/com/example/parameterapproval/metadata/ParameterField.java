package com.example.parameterapproval.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterField {
    String label();
    int order() default 0;
    boolean editable() default true;
    boolean visible() default true;
    boolean required() default false;
    boolean sensitive() default false;
}

