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
import java.time.LocalDate;

/**
 * 일일 생산실적 (선택). 누적 시 X3 와 비교.
 */
@Entity
@Table(name = "production_actual")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionActual extends BaseEntity {

    @Column(name = "production_plan_id", nullable = false)
    private Long productionPlanId;

    @Column(name = "actual_date", nullable = false)
    private LocalDate actualDate;

    @Column(name = "actual_volume_ton", precision = 18, scale = 4, nullable = false)
    private BigDecimal actualVolumeTon;

    @Column(name = "actual_defect_qty", precision = 18, scale = 4)
    private BigDecimal actualDefectQty;

    @Builder
    public ProductionActual(Long productionPlanId, LocalDate actualDate,
                            BigDecimal actualVolumeTon, BigDecimal actualDefectQty) {
        this.productionPlanId = productionPlanId;
        this.actualDate = actualDate;
        this.actualVolumeTon = actualVolumeTon;
        this.actualDefectQty = actualDefectQty;
    }
}
