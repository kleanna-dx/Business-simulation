package com.company.precost.auth.entity;

import com.company.precost.domain.enums.RoleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할-권한 매핑. (모듈 01 인증/인가)
 * <p>RoleType(8종) ↔ Permission 다대다 관계를 매핑 테이블로 표현한다.</p>
 */
@Entity
@Table(name = "role_permission",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_ROLE_PERMISSION", columnNames = {"role", "permission_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private RoleType role;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Builder
    private RolePermission(RoleType role, Long permissionId) {
        this.role = role;
        this.permissionId = permissionId;
    }
}
