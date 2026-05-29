package com.company.precost.domain.repository;

import com.company.precost.domain.entity.DashboardAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 대시보드 알림 저장소. (모듈 09 대시보드)
 */
public interface DashboardAlertRepository extends JpaRepository<DashboardAlert, Long> {

    List<DashboardAlert> findByTargetUserIdAndReadFalseOrderByCreatedAtDesc(Long targetUserId);

    long countByTargetUserIdAndReadFalse(Long targetUserId);
}
