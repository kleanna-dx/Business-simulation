package com.company.precost.auth.dto;

/**
 * 토큰 발급 응답 DTO.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummary user
) {
    public static TokenResponse of(String access, String refresh, long expiresIn, UserSummary user) {
        return new TokenResponse(access, refresh, "Bearer", expiresIn, user);
    }
}
