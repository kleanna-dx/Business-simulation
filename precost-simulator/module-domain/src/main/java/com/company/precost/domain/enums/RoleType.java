package com.company.precost.domain.enums;

import lombok.Getter;

/**
 * 8개 권한(Role). Spring Security 의 ROLE_ prefix 와 매핑.
 */
@Getter
public enum RoleType {
    USER("일반"),
    PRODUCTION("생산팀"),
    PURCHASE("구매팀"),
    COST("원가팀"),
    LOGISTICS("물류팀"),
    TEAM_LEAD("팀장"),
    FACTORY_MANAGER("공장장"),
    ADMIN("시스템관리자");

    private final String description;

    RoleType(String description) {
        this.description = description;
    }

    /** Spring Security authority 문자열 (ROLE_ prefix) */
    public String authority() {
        return "ROLE_" + name();
    }
}
