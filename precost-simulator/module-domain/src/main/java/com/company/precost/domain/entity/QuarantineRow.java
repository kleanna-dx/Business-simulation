package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검증 실패 행 격리 테이블.
 */
@Entity
@Table(name = "quarantine_row")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuarantineRow extends BaseEntity {

    @Column(name = "sync_job_id", nullable = false)
    private Long syncJobId;

    @Column(name = "raw_row", columnDefinition = "JSON", nullable = false)
    private String rawRow;

    @Column(name = "error_reason", length = 1000, nullable = false)
    private String errorReason;

    @Builder
    public QuarantineRow(Long syncJobId, String rawRow, String errorReason) {
        this.syncJobId = syncJobId;
        this.rawRow = rawRow;
        this.errorReason = errorReason;
    }
}
