package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.PriceChangeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 단가 변경. 인상율 5% 이상 시 결재선 강화(공장장 추가).
 */
@Entity
@Table(name = "price_change", indexes =
        @Index(name = "IDX_PC_MATERIAL_EFFECTIVE", columnList = "material_code,effective_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceChange extends BaseEntity {

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Column(name = "supplier_code", length = 20)
    private String supplierCode;

    @Column(name = "previous_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal previousPrice;

    @Column(name = "new_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal newPrice;

    /** (new - prev) / prev */
    @Column(name = "increase_rate", precision = 7, scale = 4, nullable = false)
    private BigDecimal increaseRate;

    /** 자사 부담율 0~1 */
    @Column(name = "burden_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal burdenRate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PriceChangeStatus status;

    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "remark", length = 500)
    private String remark;

    @Builder
    public PriceChange(String materialCode, String supplierCode, BigDecimal previousPrice,
                       BigDecimal newPrice, BigDecimal burdenRate, LocalDate effectiveDate,
                       String remark) {
        this.materialCode = materialCode;
        this.supplierCode = supplierCode;
        this.previousPrice = previousPrice;
        this.newPrice = newPrice;
        this.increaseRate = calcIncreaseRate(previousPrice, newPrice);
        this.burdenRate = burdenRate == null ? BigDecimal.ZERO : burdenRate;
        this.effectiveDate = effectiveDate;
        this.remark = remark;
        this.status = PriceChangeStatus.DRAFT;
    }

    private static BigDecimal calcIncreaseRate(BigDecimal prev, BigDecimal next) {
        if (prev == null || prev.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return next.subtract(prev).divide(prev, 4, java.math.RoundingMode.HALF_UP);
    }

    /** 인상율 5% 이상 여부 (결재선 강화 판단) */
    public boolean isMajorIncrease() {
        return increaseRate.compareTo(new BigDecimal("0.05")) >= 0;
    }

    public void submit(Long approvalId) {
        this.status = PriceChangeStatus.SUBMITTED;
        this.approvalId = approvalId;
    }

    public void approve() {
        this.status = PriceChangeStatus.APPROVED;
    }

    public void apply() {
        this.status = PriceChangeStatus.APPLIED;
    }
}
