package com.company.precost.domain.enums;

/**
 * BOM 분모 기준 (사용량 계산 시 분모톤 결정 방식).
 */
public enum DenominatorType {
    TOTAL,            // X3 전체
    EXCLUDE_JIGWAN,   // X3 - 지관 - UKP
    ACB,              // ACB 톤
    KB,               // KB 톤
    SC,
    IV,
    CCKB
}
