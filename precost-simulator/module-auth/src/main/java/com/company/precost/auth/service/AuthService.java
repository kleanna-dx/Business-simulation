package com.company.precost.auth.service;

import com.company.precost.auth.dto.*;
import com.company.precost.auth.entity.User;
import com.company.precost.auth.jwt.JwtTokenProvider;
import com.company.precost.auth.repository.RolePermissionRepository;
import com.company.precost.auth.repository.UserRepository;
import com.company.precost.common.exception.BusinessException;
import com.company.precost.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 인증 서비스 — 로그인 / 토큰 재발급 / 비밀번호 변경. (모듈 01 인증/인가)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final PasswordPolicyValidator passwordPolicyValidator;

    /** 로그인: 자격검증 → 실패 카운트/잠금 처리 → 토큰 발급. */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.onLoginFailure(loginAttemptService.getMaxAttempts());
            if (user.isLocked()) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        user.onLoginSuccess();
        return issueTokens(user);
    }

    /** Refresh Token으로 Access Token 재발급. */
    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshRequest request) {
        Claims claims = tokenProvider.parse(request.refreshToken());
        if (claims == null || !"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        User user = userRepository.findByUsername(claims.getSubject())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        return issueTokens(user);
    }

    /** 비밀번호 변경(본인). */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "현재 비밀번호가 일치하지 않습니다.");
        }
        passwordPolicyValidator.validate(request.newPassword());
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    private TokenResponse issueTokens(User user) {
        List<String> perms = rolePermissionRepository.findPermissionCodesByRole(user.getRole());
        String access = tokenProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getRole().name(), perms);
        String refresh = tokenProvider.createRefreshToken(user.getId(), user.getUsername());
        return TokenResponse.of(access, refresh,
                tokenProvider.getAccessTokenValiditySeconds(), UserSummary.from(user, perms));
    }
}
