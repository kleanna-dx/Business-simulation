package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 시뮬레이션 결과 (자재 1건). 영역 C, D 산식 결과 저장.
 */
@Entity
@Table(name = "cost_simulation_result", indexes =
        @Index(name = "IDX_CSR_SCENARIO", columnList = "scenario_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CostSimulationResult extends BaseEntity {

    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Column(name = "L_prev_unit", precision = 18, scale = 6)
    private BigDecimal lPrevUnit;

    @Column(name = "M_prev_price", precision = 18, scale = 4)
    private BigDecimal mPrevPrice;

    @Column(name = "Q_curr_unit", precision = 18, scale = 6)
    private BigDecimal qCurrUnit;

    @Column(name = "V_curr_price", precision = 18, scale = 4)
    private BigDecimal vCurrPrice;

    @Column(name = "P_usage_kg", precision = 18, scale = 4)
    private BigDecimal pUsageKg;

    @Column(name = "W_cost_million", precision = 18, scale = 4)
    private BigDecimal wCostMillion;

    @Column(name = "X_cost_per_ton", precision = 18, scale = 4)
    private BigDecimal xCostPerTon;

    @Column(name = "Y_volume_variance")
    private Long yVolumeVariance;

    @Column(name = "Z_price_variance")
    private Long zPriceVariance;

    @Column(name = "AA_total")
    private Long aaTotal;

    @Builder
    public CostSimulationResult(Long scenarioId, String materialCode, BigDecimal lPrevUnit,
                                BigDecimal mPrevPrice, BigDecimal qCurrUnit, BigDecimal vCurrPrice,
                                BigDecimal pUsageKg, BigDecimal wCostMillion, BigDecimal xCostPerTon,
                                Long yVolumeVariance, Long zPriceVariance, Long aaTotal) {
        this.scenarioId = scenarioId;
        this.materialCode = materialCode;
        this.lPrevUnit = lPrevUnit;
        this.mPrevPrice = mPrevPrice;
        this.qCurrUnit = qCurrUnit;
        this.vCurrPrice = vCurrPrice;
        this.pUsageKg = pUsageKg;
        this.wCostMillion = wCostMillion;
        this.xCostPerTon = xCostPerTon;
        this.yVolumeVariance = yVolumeVariance;
        this.zPriceVariance = zPriceVariance;
        this.aaTotal = aaTotal;
    }
}
