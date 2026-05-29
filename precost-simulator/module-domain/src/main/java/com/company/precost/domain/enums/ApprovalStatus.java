package com.company.precost.domain.enums;

/**
 * 결재 문서 전체 상태.
 */
public enum ApprovalStatus {
    DRAFTING,     // 기안 중
    IN_PROGRESS,  // 결재 진행
    APPROVED,     // 최종 승인
    REJECTED,     // 반려
    WITHDRAWN     // 회수
}
