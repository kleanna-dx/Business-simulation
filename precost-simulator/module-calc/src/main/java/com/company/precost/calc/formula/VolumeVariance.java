package com.company.precost.calc.formula;

import java.math.BigDecimal;

/**
 * 영역 D - 사용량차이 Y.
 * <pre>
 * Y = (L - Q) × M × X3
 *   L: 전월 원단위, Q: 당월 목표 원단위
 *   M: 전월 단가,  X3: 당월 생산량(톤)
 * </pre>
 * 양수 = 원단위 개선(사용량 감소)으로 인한 비용 절감.
 */
public final class VolumeVariance {

    private VolumeVariance() {
    }

    public static BigDecimal compute(BigDecimal l, BigDecimal q, BigDecimal m, BigDecimal x3) {
        if (l == null || q == null || m == null || x3 == null) {
            return BigDecimal.ZERO;
        }
        return l.subtract(q).multiply(m).multiply(x3);
    }
}
