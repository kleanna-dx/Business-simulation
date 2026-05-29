package com.company.precost.domain.enums;

/**
 * 단가 변경 상태.
 */
public enum PriceChangeStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    APPLIED,    // 적용일 도래로 단가 반영 완료
    CANCELLED
}
