package com.company.precost.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 비밀번호 변경 요청 DTO. (PUT /api/v1/auth/password)
 */
public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호는 필수입니다.") String currentPassword,
        @NotBlank(message = "새 비밀번호는 필수입니다.") String newPassword
) {}
