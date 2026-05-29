package com.company.precost.domain.repository;

import com.company.precost.domain.entity.ProductionPlan;
import com.company.precost.domain.enums.Plant;
import com.company.precost.domain.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 생산계획 저장소. (모듈 04 생산계획)
 * <p>공장 + 대상월 단위 유일성을 가진다. X3(생산량 ton)·X7(불량률)을 보관.</p>
 */
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {

    Optional<ProductionPlan> findByPlantCodeAndTargetMonth(Plant plantCode, String targetMonth);

    List<ProductionPlan> findByTargetMonth(String targetMonth);

    List<ProductionPlan> findByPlantCodeAndStatus(Plant plantCode, PlanStatus status);

    boolean existsByPlantCodeAndTargetMonth(Plant plantCode, String targetMonth);
}
