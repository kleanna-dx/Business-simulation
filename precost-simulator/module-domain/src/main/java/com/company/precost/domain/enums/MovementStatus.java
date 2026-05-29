package com.company.precost.domain.enums;

/**
 * 이동 진행 상태.
 */
public enum MovementStatus {
    PLANNED,      // 계획
    SUBMITTED,    // 결재 상신
    APPROVED,     // 승인
    IN_PROGRESS,  // 진행중
    COMPLETED,    // 완료
    CANCELLED     // 취소
}
