package com.company.precost.domain.repository;

import com.company.precost.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 감사 로그 저장소. (공통 인프라 · 모듈 09 대시보드 추적)
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTargetTypeAndTargetId(String targetType, String targetId);

    Page<AuditLog> findByChangedBy(String changedBy, Pageable pageable);
}
