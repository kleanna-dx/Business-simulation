package com.company.precost.domain.enums;

/**
 * 사용자 계정 상태.
 */
public enum UserStatus {
    ACTIVE,    // 정상
    LOCKED,    // 잠금 (로그인 실패 5회)
    INACTIVE   // 비활성 (퇴사 등)
}
