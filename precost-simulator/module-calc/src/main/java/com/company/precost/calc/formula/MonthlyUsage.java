package com.company.precost.calc.formula;

import java.math.BigDecimal;

/**
 * 영역 C - 당월 사용량 P.
 * <pre>
 * P = Q × 분모톤(material, plan)
 *   Q: 당월 목표 원단위(kg/Ton)
 *   분모톤: 자재 종류별로 결정 (DenominatorResolver)
 * </pre>
 * 결과 단위는 kg (원단위 × 톤 = kg).
 */
public final class MonthlyUsage {

    private MonthlyUsage() {
    }

    public static BigDecimal compute(BigDecimal q, BigDecimal denominatorTon) {
        if (q == null || denominatorTon == null) {
            return BigDecimal.ZERO;
        }
        return q.multiply(denominatorTon);
    }
}
