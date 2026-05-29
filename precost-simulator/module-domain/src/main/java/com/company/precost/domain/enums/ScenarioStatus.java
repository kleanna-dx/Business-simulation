package com.company.precost.domain.enums;

/**
 * 사전원가 시나리오 상태.
 */
public enum ScenarioStatus {
    DRAFT,
    RUNNING,    // 시뮬레이션 실행 중
    COMPLETED,  // 계산 완료
    SUBMITTED,  // 결재 상신
    CONFIRMED   // 공장장 확정 (잠금, 대시보드 노출)
}
