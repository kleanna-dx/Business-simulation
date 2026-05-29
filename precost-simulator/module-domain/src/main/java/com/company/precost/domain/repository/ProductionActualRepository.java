package com.company.precost.domain.repository;

import com.company.precost.domain.entity.ProductionActual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 생산실적 저장소. (모듈 04 생산계획)
 */
public interface ProductionActualRepository extends JpaRepository<ProductionActual, Long> {

    List<ProductionActual> findByProductionPlanId(Long productionPlanId);

    List<ProductionActual> findByProductionPlanIdAndActualDateBetween(
            Long productionPlanId, LocalDate from, LocalDate to);
}
