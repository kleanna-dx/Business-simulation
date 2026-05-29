package com.company.precost.domain.repository;

import com.company.precost.domain.entity.VarianceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 차이분석 리포트 저장소. (모듈 08 원가 시뮬레이션 · 모듈 09 대시보드)
 * <p>시나리오별 1건(요약 + top5).</p>
 */
public interface VarianceReportRepository extends JpaRepository<VarianceReport, Long> {

    Optional<VarianceReport> findByScenarioId(Long scenarioId);

    void deleteByScenarioId(Long scenarioId);
}
