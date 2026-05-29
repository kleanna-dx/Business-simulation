package com.company.precost.auth.dto;

import com.company.precost.auth.entity.User;

import java.util.List;

/**
 * 인증 사용자 요약 정보(클라이언트 노출용 — 비밀번호 등 민감정보 제외).
 */
public record UserSummary(
        Long id,
        String username,
        String fullName,
        String department,
        String role,
        String roleDescription,
        List<String> permissions
) {
    public static UserSummary from(User user, List<String> permissions) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getDepartment(),
                user.getRole().name(),
                user.getRole().getDescription(),
                permissions
        );
    }
}
