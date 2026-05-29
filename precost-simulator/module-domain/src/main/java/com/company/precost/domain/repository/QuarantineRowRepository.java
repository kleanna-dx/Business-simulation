package com.company.precost.domain.repository;

import com.company.precost.domain.entity.QuarantineRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 격리(quarantine) 행 저장소.
 * <p>SAP/BW 수신 데이터 검증 실패 행을 보관한다. (모듈 02 SAP 연동)</p>
 */
public interface QuarantineRowRepository extends JpaRepository<QuarantineRow, Long> {

    List<QuarantineRow> findBySyncJobId(Long syncJobId);

    long countBySyncJobId(Long syncJobId);
}
