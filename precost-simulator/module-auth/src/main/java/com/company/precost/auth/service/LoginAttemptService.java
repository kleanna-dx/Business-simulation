package com.company.precost.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 로그인 실패 임계치 관리. (모듈 01 인증/인가)
 * <p>실패 카운트 자체는 {@code users.failed_login_count} 컬럼으로 영속화되며,
 * 본 서비스는 임계치 정책 판단만 담당한다.</p>
 */
@Service
public class LoginAttemptService {

    /** 계정 잠금 임계 실패 횟수 (기본 5회). */
    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean isLockThresholdReached(int failedCount) {
        return failedCount >= maxAttempts;
    }
}
