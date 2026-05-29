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
 * 지종 마스터 (SC/IV/ACB/CCKB/KB 등).
 */
@Entity
@Table(name = "product_grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductGrade {

    @Id
    @Column(name = "grade_code", length = 20, nullable = false)
    private String gradeCode;

    @Column(name = "grade_name", length = 100, nullable = false)
    private String gradeName;

    @Column(name = "parent_category", length = 50)
    private String parentCategory;

    @Builder
    public ProductGrade(String gradeCode, String gradeName, String parentCategory) {
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.parentCategory = parentCategory;
    }

    public void update(String gradeName, String parentCategory) {
        this.gradeName = gradeName;
        this.parentCategory = parentCategory;
    }
}
