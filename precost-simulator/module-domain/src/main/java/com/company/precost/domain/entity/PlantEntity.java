package com.company.precost.domain.entity;

import com.company.precost.domain.enums.Plant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설비 마스터. plant_code(ENUM)를 PK 로 사용.
 */
@Entity
@Table(name = "plant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlantEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "plant_code", length = 10, nullable = false)
    private Plant plantCode;

    @Column(name = "plant_name", length = 100, nullable = false)
    private String plantName;

    @Builder
    public PlantEntity(Plant plantCode, String plantName) {
        this.plantCode = plantCode;
        this.plantName = plantName;
    }

    public void update(String plantName) {
        this.plantName = plantName;
    }
}
