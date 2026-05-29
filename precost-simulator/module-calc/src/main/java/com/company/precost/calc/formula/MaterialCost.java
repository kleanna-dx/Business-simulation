package com.company.precost.calc.formula;

import com.company.precost.calc.CalcConstants;

import java.math.BigDecimal;

/**
 * 영역 C - 재료비 W (백만원 단위).
 * <pre>
 * W = P × V / 1,000,000
 *   P: 사용량(kg), V: 가중평균 단가(원/kg)
 * </pre>
 */
public final class MaterialCost {

    private MaterialCost() {
    }

    public static BigDecimal compute(BigDecimal pUsageKg, BigDecimal vPrice) {
        if (pUsageKg == null || vPrice == null) {
            return BigDecimal.ZERO;
        }
        return pUsageKg.multiply(vPrice)
                .divide(CalcConstants.MILLION, CalcConstants.PRICE_SCALE, CalcConstants.ROUNDING);
    }
}
