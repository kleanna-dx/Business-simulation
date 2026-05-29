package com.company.precost.calc.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 분모톤 결정에 필요한 생산계획 톤 정보 (순수 값 객체).
 * module-calc 는 도메인 엔티티(ProductionPlan)에 의존하지 않으므로
 * cost-simulation 모듈이 이 객체로 변환하여 전달한다.
 *
 * <p>분모톤 결정 규칙(영역 C):
 * <ul>
 *   <li>펄프(11xxxxx) → X3 전체</li>
 *   <li>일반 고지(12xxxxx) → X3 - 지관 - UKP</li>
 *   <li>UKP/HINTON UKP(1100005, 1100023) → KB 톤</li>
 *   <li>ACB류 → ACB 톤</li>
 * </ul>
 */
@Getter
@Builder
public class DenominatorInput {

    /** X3: 당월 총 생산량(톤) */
    private final BigDecimal x3Total;

    /** 지관 원지 톤 (고지 분모 차감용) */
    private final BigDecimal jigwanTon;

    /** UKP 톤 (고지 분모 차감용) */
    private final BigDecimal ukpTon;

    /** KB 톤 (UKP/HINTON UKP 분모) */
    private final BigDecimal kbTon;

    /** ACB 톤 (ACB류 분모) */
    private final BigDecimal acbTon;

    public BigDecimal x3Total() {
        return orZero(x3Total);
    }

    public BigDecimal jigwanTon() {
        return orZero(jigwanTon);
    }

    public BigDecimal ukpTon() {
        return orZero(ukpTon);
    }

    public BigDecimal kbTon() {
        return orZero(kbTon);
    }

    public BigDecimal acbTon() {
        return orZero(acbTon);
    }

    private BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
