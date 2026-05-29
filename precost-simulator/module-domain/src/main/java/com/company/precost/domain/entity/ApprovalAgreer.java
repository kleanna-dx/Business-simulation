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

/**
 * 결재 합의자 (결재선과 별도, 공유받을 사람).
 */
@Entity
@Table(name = "approval_agreer", indexes =
        @Index(name = "IDX_AA_APPROVAL", columnList = "approval_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalAgreer extends BaseEntity {

    @Column(name = "approval_id", nullable = false)
    private Long approvalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    public ApprovalAgreer(Long approvalId, Long userId) {
        this.approvalId = approvalId;
        this.userId = userId;
    }
}
