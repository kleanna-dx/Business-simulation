package com.company.precost.domain.repository;

import com.company.precost.domain.entity.OpeningStock;
import com.company.precost.domain.enums.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 기초재고(T/U) 저장소. (모듈 06 입출고/물류 · 가중평균단가 V 산출 입력)
 * <p>공장 + 대상월 + 자재 단위 유일.</p>
 */
public interface OpeningStockRepository extends JpaRepository<OpeningStock, Long> {

    Optional<OpeningStock> findByPlantCodeAndTargetMonthAndMaterialCode(
            Plant plantCode, String targetMonth, String materialCode);

    List<OpeningStock> findByPlantCodeAndTargetMonth(Plant plantCode, String targetMonth);
}
