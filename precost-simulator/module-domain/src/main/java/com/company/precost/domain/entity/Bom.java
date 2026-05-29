package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.DenominatorType;
import com.company.precost.domain.enums.Plant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BOM (표준 원단위). 자재×설비×지종별 표준 원단위 + 분모기준 매핑.
 */
@Entity
@Table(name = "bom", uniqueConstraints =
        @UniqueConstraint(name = "UK_BOM_MATERIAL_PLANT_GRADE",
                columnNames = {"material_code", "plant_code", "grade_code"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bom extends BaseEntity {

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_code", length = 10, nullable = false)
    private Plant plantCode;

    @Column(name = "grade_code", length = 20, nullable = false)
    private String gradeCode;

    @Column(name = "standard_unit_consumption", precision = 18, scale = 6, nullable = false)
    private BigDecimal standardUnitConsumption;

    @Enumerated(EnumType.STRING)
    @Column(name = "denominator_type", length = 20, nullable = false)
    private DenominatorType denominatorType;

    @Builder
    public Bom(String materialCode, Plant plantCode, String gradeCode,
               BigDecimal standardUnitConsumption, DenominatorType denominatorType) {
        this.materialCode = materialCode;
        this.plantCode = plantCode;
        this.gradeCode = gradeCode;
        this.standardUnitConsumption = standardUnitConsumption;
        this.denominatorType = denominatorType;
    }

    public void update(BigDecimal standardUnitConsumption, DenominatorType denominatorType) {
        this.standardUnitConsumption = standardUnitConsumption;
        this.denominatorType = denominatorType;
    }
}
