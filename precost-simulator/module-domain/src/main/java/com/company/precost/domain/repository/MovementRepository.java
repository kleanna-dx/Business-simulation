package com.company.precost.domain.repository;

import com.company.precost.domain.entity.Movement;
import com.company.precost.domain.enums.MovementStatus;
import com.company.precost.domain.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 입출고/물류 이동 저장소. (모듈 06 입출고/물류)
 */
public interface MovementRepository extends JpaRepository<Movement, Long> {

    List<Movement> findByMaterialCode(String materialCode);

    List<Movement> findByMovementTypeAndStatus(MovementType movementType, MovementStatus status);

    List<Movement> findByPlanDateBetween(LocalDate from, LocalDate to);

    @Query("""
            select m from Movement m
            where (:type is null or m.movementType = :type)
              and (:status is null or m.status = :status)
              and (:materialCode is null or m.materialCode = :materialCode)
            """)
    Page<Movement> search(@Param("type") MovementType type,
                          @Param("status") MovementStatus status,
                          @Param("materialCode") String materialCode,
                          Pageable pageable);
}
