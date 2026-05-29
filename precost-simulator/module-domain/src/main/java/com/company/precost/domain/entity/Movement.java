package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.LocationType;
import com.company.precost.domain.enums.MovementStatus;
import com.company.precost.domain.enums.MovementType;
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
 * 이동 통합 엔티티 (입고/출고/이송/조정/출하/반품 6종).
 * 입고 R(수량), S(단가)가 사전원가 가중평균 단가 V 계산에 반영됨.
 */
@Entity
@Table(name = "movement", indexes = {
        @Index(name = "IDX_MOVEMENT_TYPE_DATE", columnList = "movement_type,plan_date"),
        @Index(name = "IDX_MOVEMENT_MATERIAL_DATE", columnList = "material_code,plan_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movement extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", length = 20, nullable = false)
    private MovementType movementType;

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_location_type", length = 20)
    private LocationType fromLocationType;

    @Column(name = "from_location_code", length = 50)
    private String fromLocationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_location_type", length = 20)
    private LocationType toLocationType;

    @Column(name = "to_location_code", length = 50)
    private String toLocationCode;

    /** R: 계획 수량(톤) */
    @Column(name = "plan_qty_ton", precision = 18, scale = 4, nullable = false)
    private BigDecimal planQtyTon;

    @Column(name = "actual_qty_ton", precision = 18, scale = 4)
    private BigDecimal actualQtyTon;

    /** S: 단가(원/kg) */
    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MovementStatus status;

    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "remark", length = 500)
    private String remark;

    @Builder
    public Movement(MovementType movementType, String materialCode,
                    LocationType fromLocationType, String fromLocationCode,
                    LocationType toLocationType, String toLocationCode,
                    BigDecimal planQtyTon, BigDecimal unitPrice, LocalDate planDate,
                    String referenceNo, String remark) {
        this.movementType = movementType;
        this.materialCode = materialCode;
        this.fromLocationType = fromLocationType;
        this.fromLocationCode = fromLocationCode;
        this.toLocationType = toLocationType;
        this.toLocationCode = toLocationCode;
        this.planQtyTon = planQtyTon;
        this.unitPrice = unitPrice;
        this.planDate = planDate;
        this.referenceNo = referenceNo;
        this.remark = remark;
        this.status = MovementStatus.PLANNED;
    }

    /** 실적 등록 */
    public void registerActual(BigDecimal actualQtyTon, LocalDate actualDate) {
        this.actualQtyTon = actualQtyTon;
        this.actualDate = actualDate;
        this.status = MovementStatus.COMPLETED;
    }

    public void submit(Long approvalId) {
        this.status = MovementStatus.SUBMITTED;
        this.approvalId = approvalId;
    }

    public void approve() {
        this.status = MovementStatus.APPROVED;
    }

    public void cancel() {
        this.status = MovementStatus.CANCELLED;
    }

    /** 계획 대비 실적 차이(톤). 실적 미등록 시 null */
    public BigDecimal variance() {
        if (actualQtyTon == null) {
            return null;
        }
        return actualQtyTon.subtract(planQtyTon);
    }
}
