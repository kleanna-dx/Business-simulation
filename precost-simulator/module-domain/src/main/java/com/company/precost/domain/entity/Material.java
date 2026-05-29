package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.ActiveStatus;
import com.company.precost.domain.enums.MaterialCategory;
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
 * 자재 마스터. material_code(7자리)가 모든 도메인의 표준 식별자.
 * <p>※ material_code 가 자연키(PK)이므로 BaseEntity 의 IDENTITY id 대신
 * 자체 @Id 를 사용한다(BaseEntity 미상속, Auditing 필드만 별도로 둠).
 */
@Entity
@Table(name = "material")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Material {

    @Id
    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Column(name = "material_name", length = 200, nullable = false)
    private String materialName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private MaterialCategory category;

    @Column(name = "chemical_group", length = 50)
    private String chemicalGroup;

    @Column(name = "unit", length = 10, nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private ActiveStatus status;

    @Builder
    public Material(String materialCode, String materialName, MaterialCategory category,
                    String chemicalGroup, String unit, ActiveStatus status) {
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.category = category;
        this.chemicalGroup = chemicalGroup;
        this.unit = unit == null ? "kg" : unit;
        this.status = status == null ? ActiveStatus.ACTIVE : status;
    }

    public void update(String materialName, MaterialCategory category,
                       String chemicalGroup, String unit) {
        this.materialName = materialName;
        this.category = category;
        this.chemicalGroup = chemicalGroup;
        this.unit = unit;
    }

    public void deactivate() {
        this.status = ActiveStatus.INACTIVE;
    }
}
