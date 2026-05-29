package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * BW Query 응답 raw 데이터 (45개 컬럼 보존, JSON).
 */
@Entity
@Table(name = "bw_raw_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BwRawSnapshot extends BaseEntity {

    @Column(name = "sync_job_id", nullable = false)
    private Long syncJobId;

    @Column(name = "raw_data", columnDefinition = "JSON", nullable = false)
    private String rawData;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Builder
    public BwRawSnapshot(Long syncJobId, String rawData, LocalDate snapshotDate) {
        this.syncJobId = syncJobId;
        this.rawData = rawData;
        this.snapshotDate = snapshotDate;
        this.processed = false;
    }

    public void markProcessed() {
        this.processed = true;
    }
}
