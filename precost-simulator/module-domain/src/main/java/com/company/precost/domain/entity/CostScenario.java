package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.Plant;
import com.company.precost.domain.enums.ScenarioStatus;
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

/**
 * 사전원가 시나리오 (Base/Plan A/Plan B 등). 확정 시 잠금.
 */
@Entity
@Table(name = "cost_scenario", indexes =
        @Index(name = "IDX_CS_PLANT_MONTH", columnList = "plant_code,target_month"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CostScenario extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_code", length = 10, nullable = false)
    private Plant plantCode;

    @Column(name = "target_month", length = 7, nullable = false)
    private String targetMonth;

    @Column(name = "scenario_name", length = 100, nullable = false)
    private String scenarioName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScenarioStatus status;

    /** 사용자가 조정한 Q/R/S 등 입력값 (JSON) */
    @Column(name = "base_inputs", columnDefinition = "JSON")
    private String baseInputs;

    @Column(name = "approval_id")
    private Long approvalId;

    @Builder
    public CostScenario(Plant plantCode, String targetMonth, String scenarioName,
                        String description, String baseInputs) {
        this.plantCode = plantCode;
        this.targetMonth = targetMonth;
        this.scenarioName = scenarioName;
        this.description = description;
        this.baseInputs = baseInputs;
        this.status = ScenarioStatus.DRAFT;
    }

    public void markRunning() {
        this.status = ScenarioStatus.RUNNING;
    }

    public void markCompleted() {
        this.status = ScenarioStatus.COMPLETED;
    }

    public void submit(Long approvalId) {
        this.status = ScenarioStatus.SUBMITTED;
        this.approvalId = approvalId;
    }

    public void confirm() {
        this.status = ScenarioStatus.CONFIRMED;
    }

    public boolean isLocked() {
        return this.status == ScenarioStatus.CONFIRMED;
    }

    public void updateInputs(String baseInputs) {
        if (isLocked()) {
            throw new IllegalStateException("확정된 시나리오는 수정할 수 없습니다.");
        }
        this.baseInputs = baseInputs;
    }
}
