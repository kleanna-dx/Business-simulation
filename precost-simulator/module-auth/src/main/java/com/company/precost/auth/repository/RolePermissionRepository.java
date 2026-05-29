package com.company.precost.auth.repository;

import com.company.precost.auth.entity.RolePermission;
import com.company.precost.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 역할-권한 매핑 저장소. (모듈 01 인증/인가)
 */
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(RoleType role);

    /** 역할이 보유한 권한 코드 목록. */
    @Query("""
            select p.code from RolePermission rp
            join Permission p on p.id = rp.permissionId
            where rp.role = :role
            """)
    List<String> findPermissionCodesByRole(@Param("role") RoleType role);
}
