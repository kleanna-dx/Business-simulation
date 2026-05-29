package com.company.precost.auth.service;

import com.company.precost.common.exception.BusinessException;
import com.company.precost.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 비밀번호 정책 검증기. (모듈 01 인증/인가)
 * <p>정책: 8자 이상, 영문 대/소문자·숫자·특수문자 중 3종 이상 포함, 공백 불가.</p>
 */
@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER   = Pattern.compile("[A-Z]");
    private static final Pattern LOWER   = Pattern.compile("[a-z]");
    private static final Pattern DIGIT   = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PASSWORD_POLICY_VIOLATION,
                    "비밀번호는 최소 " + MIN_LENGTH + "자 이상이어야 합니다.");
        }
        if (rawPassword.contains(" ")) {
            throw new BusinessException(ErrorCode.PASSWORD_POLICY_VIOLATION,
                    "비밀번호에 공백을 포함할 수 없습니다.");
        }
        int kinds = 0;
        if (UPPER.matcher(rawPassword).find())   kinds++;
        if (LOWER.matcher(rawPassword).find())   kinds++;
        if (DIGIT.matcher(rawPassword).find())   kinds++;
        if (SPECIAL.matcher(rawPassword).find()) kinds++;
        if (kinds < 3) {
            throw new BusinessException(ErrorCode.PASSWORD_POLICY_VIOLATION,
                    "비밀번호는 영문 대/소문자, 숫자, 특수문자 중 3종 이상을 포함해야 합니다.");
        }
    }
}
