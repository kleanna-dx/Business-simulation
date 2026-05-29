package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.ApprovalStatus;
import com.company.precost.domain.enums.ApprovalTargetType;
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

import java.time.LocalDateTime;

/**
 * 결재 문서 (다형성). target_type/target_id 로 어떤 도메인이든 결재 가능.
 */
@Entity
@Table(name = "approval", indexes = {
        @Index(name = "IDX_APPROVAL_TARGET", columnList = "target_type,target_id"),
        @Index(name = "IDX_APPROVAL_DRAFTER", columnList = "drafter_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Approval extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30, nullable = false)
    private ApprovalTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "drafter_id", nullable = false)
    private Long drafterId;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ApprovalStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Builder
    public Approval(ApprovalTargetType targetType, Long targetId, String title, Long drafterId) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.title = title;
        this.drafterId = drafterId;
        this.currentStep = 1;
        this.status = ApprovalStatus.IN_PROGRESS;
        this.submittedAt = LocalDateTime.now();
    }

    public void advanceStep() {
        this.currentStep++;
    }

    public void approveFinal() {
        this.status = ApprovalStatus.APPROVED;
        this.finishedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ApprovalStatus.REJECTED;
        this.finishedAt = LocalDateTime.now();
    }

    public void withdraw() {
        this.status = ApprovalStatus.WITHDRAWN;
        this.finishedAt = LocalDateTime.now();
    }

    public boolean isInProgress() {
        return this.status == ApprovalStatus.IN_PROGRESS;
    }
}
