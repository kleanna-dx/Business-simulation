package com.company.precost.domain.entity;

import com.company.precost.domain.enums.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이동유형 마스터.
 */
@Entity
@Table(name = "movement_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovementTypeEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", length = 20, nullable = false)
    private MovementType typeCode;

    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @Column(name = "is_inbound", nullable = false)
    private boolean inbound;

    @Builder
    public MovementTypeEntity(MovementType typeCode, String description, boolean inbound) {
        this.typeCode = typeCode;
        this.description = description;
        this.inbound = inbound;
    }
}
