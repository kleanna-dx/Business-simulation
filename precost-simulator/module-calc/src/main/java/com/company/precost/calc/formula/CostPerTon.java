package com.company.precost.calc.formula;

import com.company.precost.calc.CalcConstants;

import java.math.BigDecimal;

/**
 * 영역 C - 톤당비용 X.
 * <pre>
 * X = W / X3 × 1,000,000
 *   W: 재료비(백만원), X3: 당월 생산량(톤)
 * </pre>
 * 결과 단위는 원/톤.
 * 경계: X3 가 0 이면 0.
 */
public final class CostPerTon {

    private CostPerTon() {
    }

    public static BigDecimal compute(BigDecimal wCostMillion, BigDecimal x3ProductionTon) {
        if (wCostMillion == null || x3ProductionTon == null || x3ProductionTon.signum() == 0) {
            return BigDecimal.ZERO.setScale(CalcConstants.PRICE_SCALE, CalcConstants.ROUNDING);
        }
        return wCostMillion.multiply(CalcConstants.MILLION)
                .divide(x3ProductionTon, CalcConstants.PRICE_SCALE, CalcConstants.ROUNDING);
    }
}
