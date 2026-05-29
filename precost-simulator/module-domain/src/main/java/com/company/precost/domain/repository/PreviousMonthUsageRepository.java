package com.company.precost.domain.repository;

import com.company.precost.domain.entity.PreviousMonthUsage;
import com.company.precost.domain.enums.Plant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 전월 사용현황 Repository. 사전원가 베이스 데이터 조회.
 */
public interface PreviousMonthUsageRepository extends JpaRepository<PreviousMonthUsage, Long> {

    /** 멱등 적재용 자연키 조회 */
    Optional<PreviousMonthUsage> findByPlantCodeAndTargetMonthAndMaterialCodeAndGradeCodeAndBasisWeight(
            Plant plantCode, String targetMonth, String materialCode, String gradeCode, BigDecimal basisWeight);

    /** 시뮬레이션 베이스: 설비×월 전체 */
    List<PreviousMonthUsage> findByPlantCodeAndTargetMonth(Plant plantCode, String targetMonth);

    /** 다단 필터 조회 */
    @Query("select p from PreviousMonthUsage p " +
            "where p.plantCode = :plant and p.targetMonth = :month " +
            "and (:gradeCode is null or p.gradeCode = :gradeCode) " +
            "and (:materialName is null or p.materialCode like %:materialName%)")
    Page<PreviousMonthUsage> searchUsage(@Param("plant") Plant plant,
                                         @Param("month") String month,
                                         @Param("gradeCode") String gradeCode,
                                         @Param("materialName") String materialName,
                                         Pageable pageable);
}
