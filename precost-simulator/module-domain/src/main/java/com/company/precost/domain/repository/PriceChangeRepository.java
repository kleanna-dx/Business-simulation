package com.company.precost.domain.repository;

import com.company.precost.domain.entity.PriceChange;
import com.company.precost.domain.enums.PriceChangeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 단가변동 저장소. (모듈 07 단가변동)
 * <p>5% 초과 인상 시 전결 라우팅 대상.</p>
 */
public interface PriceChangeRepository extends JpaRepository<PriceChange, Long> {

    List<PriceChange> findByMaterialCode(String materialCode);

    List<PriceChange> findByStatus(PriceChangeStatus status);

    List<PriceChange> findByEffectiveDateBetween(LocalDate from, LocalDate to);

    @Query("""
            select pc from PriceChange pc
            where (:materialCode is null or pc.materialCode = :materialCode)
              and (:status is null or pc.status = :status)
            """)
    Page<PriceChange> search(@Param("materialCode") String materialCode,
                             @Param("status") PriceChangeStatus status,
                             Pageable pageable);
}
