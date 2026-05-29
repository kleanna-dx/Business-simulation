package com.company.precost.domain.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.PriceSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 자재별 단가 시계열 이력.
 */
@Entity
@Table(name = "price_history", indexes =
        @Index(name = "IDX_PH_MATERIAL_EFFECTIVE", columnList = "material_code,effective_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceHistory extends BaseEntity {

    @Column(name = "material_code", length = 7, nullable = false)
    private String materialCode;

    @Column(name = "price", precision = 18, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    private PriceSource source;

    @Builder
    public PriceHistory(String materialCode, BigDecimal price, LocalDate effectiveDate, PriceSource source) {
        this.materialCode = materialCode;
        this.price = price;
        this.effectiveDate = effectiveDate;
        this.source = source;
    }
}
