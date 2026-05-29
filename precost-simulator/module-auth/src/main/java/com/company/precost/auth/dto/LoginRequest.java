package com.company.precost.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO. (POST /api/v1/auth/login)
 */
public record LoginRequest(
        @NotBlank(message = "아이디는 필수입니다.") String username,
        @NotBlank(message = "비밀번호는 필수입니다.") String password
) {}
