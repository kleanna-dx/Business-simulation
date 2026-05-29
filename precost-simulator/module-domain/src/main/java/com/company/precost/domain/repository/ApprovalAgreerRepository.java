package com.company.precost.domain.repository;

import com.company.precost.domain.entity.ApprovalAgreer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 결재 합의자 저장소. (모듈 10 전자결재)
 */
public interface ApprovalAgreerRepository extends JpaRepository<ApprovalAgreer, Long> {

    List<ApprovalAgreer> findByApprovalId(Long approvalId);
}
