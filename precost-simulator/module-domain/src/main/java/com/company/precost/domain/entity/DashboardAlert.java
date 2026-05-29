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

import java.time.LocalDateTime;

/**
 * 대시보드 알림 (재고부족/단가인상/결재대기/폐품율초과 등).
 */
@Entity
@Table(name = "dashboard_alert", indexes =
        @Index(name = "IDX_DA_USER_READ", columnList = "target_user_id,is_read"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardAlert extends BaseEntity {

    @Column(name = "alert_type", length = 30, nullable = false)
    private String alertType;

    @Column(name = "severity", length = 10, nullable = false)
    private String severity;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "target_url", length = 300)
    private String targetUrl;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder
    public DashboardAlert(String alertType, String severity, String title, String message,
                          String targetUrl, Long targetUserId, LocalDateTime expiresAt) {
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.targetUrl = targetUrl;
        this.targetUserId = targetUserId;
        this.expiresAt = expiresAt;
        this.read = false;
    }

    public void markRead() {
        this.read = true;
    }
}
