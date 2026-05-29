package com.company.precost.domain.repository;

import com.company.precost.domain.entity.Approval;
import com.company.precost.domain.enums.ApprovalStatus;
import com.company.precost.domain.enums.ApprovalTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 전자결재 저장소. (모듈 10 전자결재)
 * <p>다형성(target_type/target_id)으로 단가변동·생산계획·시나리오 등을 결재한다.</p>
 */
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    Optional<Approval> findByTargetTypeAndTargetId(ApprovalTargetType targetType, Long targetId);

    List<Approval> findByDrafterId(Long drafterId);

    List<Approval> findByStatus(ApprovalStatus status);

    /** 특정 결재자가 처리해야 할 진행 중 문서(현재 단계 승인 대기). */
    @Query("""
            select a from Approval a
            join ApprovalStep s on s.approvalId = a.id
            where a.status = com.company.precost.domain.enums.ApprovalStatus.IN_PROGRESS
              and s.stepOrder = a.currentStep
              and s.approverId = :approverId
              and s.action = com.company.precost.domain.enums.ApprovalStepAction.PENDING
            """)
    Page<Approval> findPendingForApprover(@Param("approverId") Long approverId, Pageable pageable);
}
