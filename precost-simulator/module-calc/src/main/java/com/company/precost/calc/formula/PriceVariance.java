package com.company.precost.calc.formula;

import java.math.BigDecimal;

/**
 * 영역 D - 단가차이 Z.
 * <pre>
 * Z = Q × (M - V) × X3
 *   Q: 당월 목표 원단위, M: 전월 단가
 *   V: 당월 가중평균 단가, X3: 당월 생산량(톤)
 * </pre>
 * 양수 = 단가 하락으로 인한 비용 절감, 음수 = 단가 상승(악화).
 */
public final class PriceVariance {

    private PriceVariance() {
    }

    public static BigDecimal compute(BigDecimal q, BigDecimal m, BigDecimal v, BigDecimal x3) {
        if (q == null || m == null || v == null || x3 == null) {
            return BigDecimal.ZERO;
        }
        return q.multiply(m.subtract(v)).multiply(x3);
    }
}
