package com.company.precost.auth.controller;

import com.company.precost.auth.dto.*;
import com.company.precost.auth.security.CustomUserDetails;
import com.company.precost.auth.service.AuthService;
import com.company.precost.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API. (모듈 01 인증/인가)
 * <pre>
 *   POST /api/v1/auth/login    로그인(토큰 발급)
 *   POST /api/v1/auth/refresh  토큰 재발급
 *   PUT  /api/v1/auth/password 비밀번호 변경(본인)
 *   GET  /api/v1/auth/me       내 정보
 * </pre>
 */
@Tag(name = "Auth", description = "인증/토큰 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "아이디/비밀번호로 Access·Refresh 토큰을 발급한다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh 토큰으로 Access 토큰을 재발급한다.")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자가 본인 비밀번호를 변경한다.")
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUserId(), request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "내 정보", description = "현재 로그인한 사용자의 요약 정보를 반환한다.")
    @GetMapping("/me")
    public ApiResponse<UserSummary> me(@AuthenticationPrincipal CustomUserDetails principal) {
        UserSummary summary = new UserSummary(
                principal.getUserId(), principal.getUsername(), null, null,
                principal.getAuthorities().stream()
                        .map(Object::toString)
                        .filter(a -> a.startsWith("ROLE_"))
                        .findFirst().orElse("ROLE_USER"),
                null,
                principal.getAuthorities().stream()
                        .map(Object::toString)
                        .filter(a -> !a.startsWith("ROLE_"))
                        .toList());
        return ApiResponse.success(summary);
    }
}
