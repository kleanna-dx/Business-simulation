package com.company.precost.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 재발급 요청 DTO. (POST /api/v1/auth/refresh)
 */
public record RefreshRequest(
        @NotBlank(message = "refreshToken은 필수입니다.") String refreshToken
) {}
