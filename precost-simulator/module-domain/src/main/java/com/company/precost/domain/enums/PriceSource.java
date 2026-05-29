package com.company.precost.domain.enums;

/**
 * 단가 이력 출처.
 */
public enum PriceSource {
    INITIAL,       // 최초 등록
    PRICE_CHANGE,  // 단가 변경
    SAP_SYNC       // SAP 연동
}
