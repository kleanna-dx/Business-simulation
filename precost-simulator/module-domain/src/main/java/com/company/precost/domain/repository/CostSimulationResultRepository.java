package com.company.precost.domain.repository;

import com.company.precost.domain.entity.CostSimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시뮬레이션 결과(자재별 산식 결과) 저장소. (모듈 08 원가 시뮬레이션)
 * <p>L/M/Q/V/P/W/X/Y/Z/AA 산식 산출값 보관.</p>
 */
public interface CostSimulationResultRepository extends JpaRepository<CostSimulationResult, Long> {

    List<CostSimulationResult> findByScenarioId(Long scenarioId);

    /** 재시뮬레이션 시 기존 결과 일괄 삭제(idempotent 재계산). */
    @Transactional
    void deleteByScenarioId(Long scenarioId);
}
