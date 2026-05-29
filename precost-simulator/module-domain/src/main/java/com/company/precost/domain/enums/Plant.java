package com.company.precost.domain.enums;

import lombok.Getter;

/**
 * 생산 설비/호기. 화장지는 TISSUE.
 */
@Getter
public enum Plant {
    PM2("PM2 백판지 2호기"),
    PM3("PM3 백판지 3호기"),
    TISSUE("화장지 설비");

    private final String description;

    Plant(String description) {
        this.description = description;
    }
}
