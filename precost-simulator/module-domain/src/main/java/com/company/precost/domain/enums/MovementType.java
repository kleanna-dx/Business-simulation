package com.company.precost.domain.enums;

import lombok.Getter;

/**
 * 이동 유형 (6종 통합).
 * 새 유형 추가 시 이 ENUM 만 확장하면 화면/API/결재 플로우 재사용 가능.
 */
@Getter
public enum MovementType {
    INBOUND("입고", true),
    OUTBOUND("출고", false),
    TRANSFER("이송", false),
    ADJUSTMENT("조정", false),
    SHIPMENT("출하", false),
    RETURN("반품", false);

    private final String description;
    private final boolean inbound;

    MovementType(String description, boolean inbound) {
        this.description = description;
        this.inbound = inbound;
    }
}
