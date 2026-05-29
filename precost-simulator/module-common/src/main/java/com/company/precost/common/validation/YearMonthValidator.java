package com.company.precost.common.validation;

import com.company.precost.common.util.YearMonthUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link YearMonthFormat} 검증 구현체.
 * null 은 통과시킨다(@NotNull 과 조합하여 사용).
 */
public class YearMonthValidator implements ConstraintValidator<YearMonthFormat, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return YearMonthUtils.isValid(value);
    }
}
