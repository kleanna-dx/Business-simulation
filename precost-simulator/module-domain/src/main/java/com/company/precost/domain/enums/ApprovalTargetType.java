package com.company.precost.domain.enums;

/**
 * 결재 대상 유형 (다형성). 어떤 도메인이 결재 대상인지 식별.
 */
public enum ApprovalTargetType {
    PRODUCTION_PLAN,
    MOVEMENT,
    PRICE_CHANGE,
    COST_SCENARIO,
    MASTER_CHANGE,
    MOVEMENT_ACTUAL
}
