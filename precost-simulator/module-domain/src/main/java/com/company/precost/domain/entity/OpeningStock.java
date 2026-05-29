package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.Plant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 기초재고 (전월 기말 자동이월). T(수량), U(단가) → 가중평균 단가 V 계산에 사용.
 */
@Entity
@Table(name = "opening_stock",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_OS_PLANT_MONTH_MATERIAL",
                columnNames = {"plant_code", "target_month", "material_code"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpeningStock extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_code", length = 10, nullable = false)
    private Plant plantCode;

    @Column(name = "target_month", length = 7, nullable = false)
    private String targetMonth;

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    /** T: 기초재고(톤) */
    @Column(name = "opening_qty_ton", precision = 18, scale = 4, nullable = false)
    private BigDecimal openingQtyTon;

    /** U: 기초재고 단가(원/kg) */
    @Column(name = "opening_unit_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal openingUnitPrice;

    @Builder
    public OpeningStock(Plant plantCode, String targetMonth, String materialCode,
                        BigDecimal openingQtyTon, BigDecimal openingUnitPrice) {
        this.plantCode = plantCode;
        this.targetMonth = targetMonth;
        this.materialCode = materialCode;
        this.openingQtyTon = openingQtyTon;
        this.openingUnitPrice = openingUnitPrice;
    }

    public void update(BigDecimal openingQtyTon, BigDecimal openingUnitPrice) {
        this.openingQtyTon = openingQtyTon;
        this.openingUnitPrice = openingUnitPrice;
    }
}
