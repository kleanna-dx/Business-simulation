package com.company.precost.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 'YYYY-MM' 형식의 target_month 필드 검증 어노테이션.
 */
@Documented
@Constraint(validatedBy = YearMonthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface YearMonthFormat {

    String message() default "기준월 형식이 올바르지 않습니다(YYYY-MM).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
