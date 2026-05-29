package com.company.precost.domain.repository;

import com.company.precost.domain.entity.BwRawSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BwRawSnapshotRepository extends JpaRepository<BwRawSnapshot, Long> {

    List<BwRawSnapshot> findByProcessedFalse();

    List<BwRawSnapshot> findBySyncJobId(Long syncJobId);
}
