package com.company.precost.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 권한(기능 단위). (모듈 01 인증/인가)
 * <p>예: MATERIAL_READ, COST_SCENARIO_WRITE, APPROVAL_PROCESS 등.</p>
 */
@Entity
@Table(name = "permission",
        uniqueConstraints = @UniqueConstraint(name = "UK_PERMISSION_CODE", columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "description", length = 200)
    private String description;

    @Builder
    private Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
