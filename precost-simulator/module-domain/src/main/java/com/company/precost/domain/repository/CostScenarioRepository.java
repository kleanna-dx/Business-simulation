package com.company.precost.domain.repository;

import com.company.precost.domain.entity.CostScenario;
import com.company.precost.domain.enums.Plant;
import com.company.precost.domain.enums.ScenarioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 사전원가 시뮬레이션 시나리오 저장소. (모듈 08 원가 시뮬레이션)
 */
public interface CostScenarioRepository extends JpaRepository<CostScenario, Long> {

    List<CostScenario> findByPlantCodeAndTargetMonth(Plant plantCode, String targetMonth);

    List<CostScenario> findByStatus(ScenarioStatus status);

    @Query("""
            select s from CostScenario s
            where (:plant is null or s.plantCode = :plant)
              and (:month is null or s.targetMonth = :month)
              and (:status is null or s.status = :status)
            """)
    Page<CostScenario> search(@Param("plant") Plant plant,
                              @Param("month") String month,
                              @Param("status") ScenarioStatus status,
                              Pageable pageable);
}
