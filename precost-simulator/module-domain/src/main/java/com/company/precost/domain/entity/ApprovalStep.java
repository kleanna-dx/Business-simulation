package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.ApprovalStepAction;
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
 * 결재선 단계. step_order 순서대로 진행.
 */
@Entity
@Table(name = "approval_step", indexes =
        @Index(name = "IDX_AS_APPROVAL", columnList = "approval_id,step_order"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalStep extends BaseEntity {

    @Column(name = "approval_id", nullable = false)
    private Long approvalId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Column(name = "role", length = 50)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20, nullable = false)
    private ApprovalStepAction action;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "delegated_to_id")
    private Long delegatedToId;

    @Builder
    public ApprovalStep(Long approvalId, int stepOrder, Long approverId, String role) {
        this.approvalId = approvalId;
        this.stepOrder = stepOrder;
        this.approverId = approverId;
        this.role = role;
        this.action = ApprovalStepAction.PENDING;
    }

    public void approve(String comment) {
        this.action = ApprovalStepAction.APPROVED;
        this.comment = comment;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(String comment) {
        this.action = ApprovalStepAction.REJECTED;
        this.comment = comment;
        this.processedAt = LocalDateTime.now();
    }

    public void delegate(Long delegatedToId, String comment) {
        this.action = ApprovalStepAction.DELEGATED;
        this.delegatedToId = delegatedToId;
        this.comment = comment;
        this.processedAt = LocalDateTime.now();
    }
}
