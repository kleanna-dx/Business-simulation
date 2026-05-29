package com.company.precost.domain.enums;

/**
 * 결재 단계별 처리 상태.
 */
public enum ApprovalStepAction {
    PENDING,    // 대기
    APPROVED,   // 승인
    REJECTED,   // 반려
    DELEGATED   // 위임
}
