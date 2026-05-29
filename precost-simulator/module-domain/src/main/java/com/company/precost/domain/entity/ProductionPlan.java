package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.Plant;
import com.company.precost.domain.enums.PlanStatus;
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
import java.time.LocalDateTime;

/**
 * 월간 생산계획. 설비×월별 1건. X3/X7/원지구분 톤이 시뮬레이션 분모로 사용됨.
 */
@Entity
@Table(name = "production_plan",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_PP_PLANT_MONTH", columnNames = {"plant_code", "target_month"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionPlan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_code", length = 10, nullable = false)
    private Plant plantCode;

    @Column(name = "target_month", length = 7, nullable = false)
    private String targetMonth;

    /** X3: 당월 생산량(톤) */
    @Column(name = "production_volume_ton", precision = 18, scale = 4, nullable = false)
    private BigDecimal productionVolumeTon;

    /** X7: 당월 폐품율 (0.0~0.3) */
    @Column(name = "defect_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal defectRate;

    /** 원지구분 톤 (JSON). 예: {"ACB":2000,"KB":4000,"SC":10000} */
    @Column(name = "grade_breakdown", columnDefinition = "JSON")
    private String gradeBreakdown;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PlanStatus status;

    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder
    public ProductionPlan(Plant plantCode, String targetMonth, BigDecimal productionVolumeTon,
                          BigDecimal defectRate, String gradeBreakdown) {
        this.plantCode = plantCode;
        this.targetMonth = targetMonth;
        this.productionVolumeTon = productionVolumeTon;
        this.defectRate = defectRate;
        this.gradeBreakdown = gradeBreakdown;
        this.status = PlanStatus.DRAFT;
    }

    public void update(BigDecimal productionVolumeTon, BigDecimal defectRate, String gradeBreakdown) {
        ensureDraft();
        this.productionVolumeTon = productionVolumeTon;
        this.defectRate = defectRate;
        this.gradeBreakdown = gradeBreakdown;
    }

    public void submit(Long approvalId) {
        ensureDraft();
        this.status = PlanStatus.SUBMITTED;
        this.approvalId = approvalId;
        this.submittedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = PlanStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public boolean isDraft() {
        return this.status == PlanStatus.DRAFT;
    }

    private void ensureDraft() {
        if (this.status != PlanStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 수정할 수 있습니다. 현재: " + status);
        }
    }
}
