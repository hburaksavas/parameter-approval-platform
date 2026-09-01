package com.example.parameterapproval.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterField {
    FilterOperator[] operators() default {FilterOperator.EQ};
    FilterInput input() default FilterInput.TEXT;
}

