package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 시나리오별 변동분석 요약 (폭포차트/KPI 용).
 */
@Entity
@Table(name = "variance_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VarianceReport extends BaseEntity {

    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    @Column(name = "total_cost_per_ton", precision = 18, scale = 4)
    private BigDecimal totalCostPerTon;

    @Column(name = "total_material_cost")
    private Long totalMaterialCost;

    @Column(name = "volume_variance_total")
    private Long volumeVarianceTotal;

    @Column(name = "price_variance_total")
    private Long priceVarianceTotal;

    /** 손익기여 TOP5 자재 (JSON) */
    @Column(name = "top5_materials", columnDefinition = "JSON")
    private String top5Materials;

    @Builder
    public VarianceReport(Long scenarioId, BigDecimal totalCostPerTon, Long totalMaterialCost,
                          Long volumeVarianceTotal, Long priceVarianceTotal, String top5Materials) {
        this.scenarioId = scenarioId;
        this.totalCostPerTon = totalCostPerTon;
        this.totalMaterialCost = totalMaterialCost;
        this.volumeVarianceTotal = volumeVarianceTotal;
        this.priceVarianceTotal = priceVarianceTotal;
        this.top5Materials = top5Materials;
    }
}
