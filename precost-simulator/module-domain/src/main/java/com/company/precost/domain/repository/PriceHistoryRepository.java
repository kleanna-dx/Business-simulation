package com.company.precost.domain.repository;

import com.company.precost.domain.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 단가 이력 저장소. (모듈 07 단가변동)
 * <p>특정 시점 유효 단가를 조회한다.</p>
 */
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByMaterialCodeOrderByEffectiveDateDesc(String materialCode);

    /** 지정일 이전(포함)에 유효한 가장 최근 단가 1건. */
    Optional<PriceHistory> findFirstByMaterialCodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            String materialCode, LocalDate effectiveDate);
}
