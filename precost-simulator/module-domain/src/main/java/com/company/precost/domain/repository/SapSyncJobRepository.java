package com.company.precost.domain.repository;

import com.company.precost.domain.entity.SapSyncJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SapSyncJobRepository extends JpaRepository<SapSyncJob, Long> {

    Page<SapSyncJob> findAllByOrderByStartedAtDesc(Pageable pageable);
}
