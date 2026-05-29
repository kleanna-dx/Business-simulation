package com.company.precost.auth.dto;

import com.company.precost.domain.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 사용자 생성 요청 DTO. (POST /api/v1/users — ADMIN 전용)
 */
public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank String password,
        @NotBlank @Size(max = 50) String fullName,
        @Email String email,
        @Size(max = 50) String department,
        @NotNull RoleType role
) {}
