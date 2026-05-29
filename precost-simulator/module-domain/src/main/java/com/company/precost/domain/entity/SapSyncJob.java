package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.SyncStatus;
import com.company.precost.domain.enums.SyncType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SAP 동기화 작업 이력.
 */
@Entity
@Table(name = "sap_sync_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SapSyncJob extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", length = 20, nullable = false)
    private SyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private SyncStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "record_count")
    private Integer recordCount;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Builder
    public SapSyncJob(SyncType syncType, SyncStatus status, LocalDateTime startedAt) {
        this.syncType = syncType;
        this.status = status == null ? SyncStatus.RUNNING : status;
        this.startedAt = startedAt == null ? LocalDateTime.now() : startedAt;
    }

    public void success(int recordCount) {
        this.status = SyncStatus.SUCCESS;
        this.recordCount = recordCount;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = SyncStatus.FAILED;
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
    }
}
