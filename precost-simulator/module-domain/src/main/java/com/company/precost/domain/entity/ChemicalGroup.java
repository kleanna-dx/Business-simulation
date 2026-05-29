package com.company.precost.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약품그룹 마스터 (LATEX류, 탄산칼슘류 등).
 */
@Entity
@Table(name = "chemical_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChemicalGroup {

    @Id
    @Column(name = "group_code", length = 50, nullable = false)
    private String groupCode;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Builder
    public ChemicalGroup(String groupCode, String groupName) {
        this.groupCode = groupCode;
        this.groupName = groupName;
    }

    public void update(String groupName) {
        this.groupName = groupName;
    }
}
