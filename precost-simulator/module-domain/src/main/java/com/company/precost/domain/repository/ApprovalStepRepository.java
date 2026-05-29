package com.company.precost.domain.repository;

import com.company.precost.domain.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 결재 단계(라인) 저장소. (모듈 10 전자결재)
 */
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByApprovalIdOrderByStepOrderAsc(Long approvalId);

    Optional<ApprovalStep> findByApprovalIdAndStepOrder(Long approvalId, int stepOrder);
}
