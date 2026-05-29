package com.company.precost.calc.formula;

import java.math.BigDecimal;

/**
 * 영역 D - 재료비종합 AA.
 * <pre>
 * AA = Y + Z
 *   Y: 사용량차이, Z: 단가차이
 * </pre>
 * 양수 = 개선, 음수 = 악화.
 */
public final class TotalVariance {

    private TotalVariance() {
    }

    public static BigDecimal sum(BigDecimal y, BigDecimal z) {
        BigDecimal yy = y == null ? BigDecimal.ZERO : y;
        BigDecimal zz = z == null ? BigDecimal.ZERO : z;
        return yy.add(zz);
    }
}
