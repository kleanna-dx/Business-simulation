package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.Plant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 정제된 전월 실적 (영역 A). 사전원가 시뮬레이션의 베이스.
 * <p>K=prevUsageKg, L=prevUnitConsumption, M=prevUnitPrice, N=prevMaterialCost.
 */
@Entity
@Table(name = "previous_month_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_PMU_PLANT_MONTH_MATERIAL_GRADE_BW",
                columnNames = {"plant_code", "target_month", "material_code", "grade_code", "basis_weight"}),
        indexes = {
                @Index(name = "IDX_PMU_PLANT_MONTH", columnList = "plant_code,target_month"),
                @Index(name = "IDX_PMU_MATERIAL", columnList = "material_code")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreviousMonthUsage extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_code", length = 10, nullable = false)
    private Plant plantCode;

    @Column(name = "target_month", length = 7, nullable = false)
    private String targetMonth;

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Column(name = "grade_code", length = 20, nullable = false)
    private String gradeCode;

    @Column(name = "basis_weight", precision = 10, scale = 2, nullable = false)
    private BigDecimal basisWeight;

    /** K: 전월 사용량(kg) */
    @Column(name = "prev_usage_kg", precision = 18, scale = 4)
    private BigDecimal prevUsageKg;

    /** L: 전월 원단위(kg/Ton) */
    @Column(name = "prev_unit_consumption", precision = 18, scale = 6)
    private BigDecimal prevUnitConsumption;

    /** M: 전월 단가(원/kg) */
    @Column(name = "prev_unit_price", precision = 18, scale = 4)
    private BigDecimal prevUnitPrice;

    /** N: 전월 재료비(원) */
    @Column(name = "prev_material_cost")
    private Long prevMaterialCost;

    @Column(name = "prev_total_production", precision = 18, scale = 4)
    private BigDecimal prevTotalProduction;

    @Column(name = "prev_defect_rate", precision = 5, scale = 4)
    private BigDecimal prevDefectRate;

    /** 검증용: BW 가 계산한 전월대비 사용량차이 */
    @Column(name = "bw_volume_variance")
    private Long bwVolumeVariance;

    /** 검증용: BW 가 계산한 전월대비 단가차이 */
    @Column(name = "bw_price_variance")
    private Long bwPriceVariance;

    @Builder
    public PreviousMonthUsage(Plant plantCode, String targetMonth, String materialCode,
                              String gradeCode, BigDecimal basisWeight, BigDecimal prevUsageKg,
                              BigDecimal prevUnitConsumption, BigDecimal prevUnitPrice,
                              Long prevMaterialCost, BigDecimal prevTotalProduction,
                              BigDecimal prevDefectRate, Long bwVolumeVariance, Long bwPriceVariance) {
        this.plantCode = plantCode;
        this.targetMonth = targetMonth;
        this.materialCode = materialCode;
        this.gradeCode = gradeCode;
        this.basisWeight = basisWeight;
        this.prevUsageKg = prevUsageKg;
        this.prevUnitConsumption = prevUnitConsumption;
        this.prevUnitPrice = prevUnitPrice;
        this.prevMaterialCost = prevMaterialCost;
        this.prevTotalProduction = prevTotalProduction;
        this.prevDefectRate = prevDefectRate;
        this.bwVolumeVariance = bwVolumeVariance;
        this.bwPriceVariance = bwPriceVariance;
    }

    /** 멱등 재적재 시 값 갱신 (UPSERT 패턴) */
    public void overwrite(BigDecimal prevUsageKg, BigDecimal prevUnitConsumption,
                          BigDecimal prevUnitPrice, Long prevMaterialCost,
                          BigDecimal prevTotalProduction, BigDecimal prevDefectRate,
                          Long bwVolumeVariance, Long bwPriceVariance) {
        this.prevUsageKg = prevUsageKg;
        this.prevUnitConsumption = prevUnitConsumption;
        this.prevUnitPrice = prevUnitPrice;
        this.prevMaterialCost = prevMaterialCost;
        this.prevTotalProduction = prevTotalProduction;
        this.prevDefectRate = prevDefectRate;
        this.bwVolumeVariance = bwVolumeVariance;
        this.bwPriceVariance = bwPriceVariance;
    }
}
