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
 * 변경 이력 (마스터 변경 추적). AuditAspect 가 자동 기록.
 */
@Entity
@Table(name = "audit_log", indexes =
        @Index(name = "IDX_AUDIT_TARGET", columnList = "target_type,target_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog extends BaseEntity {

    @Column(name = "target_type", length = 50, nullable = false)
    private String targetType;

    @Column(name = "target_id", length = 50, nullable = false)
    private String targetId;

    @Column(name = "action", length = 20, nullable = false)
    private String action;

    @Column(name = "before_data", columnDefinition = "JSON")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "JSON")
    private String afterData;

    @Column(name = "changed_by", length = 50)
    private String changedBy;

    @Builder
    public AuditLog(String targetType, String targetId, String action,
                    String beforeData, String afterData, String changedBy) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.action = action;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.changedBy = changedBy;
    }
}
