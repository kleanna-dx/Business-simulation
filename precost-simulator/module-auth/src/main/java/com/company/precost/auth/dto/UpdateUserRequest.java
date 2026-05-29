package com.company.precost.auth.dto;

import com.company.precost.domain.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 사용자 수정 요청 DTO. (PUT /api/v1/users/{id} — ADMIN 전용)
 * <p>null 필드는 변경하지 않는다(부분 수정).</p>
 */
public record UpdateUserRequest(
        @Size(max = 50) String fullName,
        @Email String email,
        @Size(max = 50) String department,
        RoleType role
) {}
