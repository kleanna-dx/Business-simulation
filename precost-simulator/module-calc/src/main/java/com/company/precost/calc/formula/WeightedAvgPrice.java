package com.company.precost.calc.formula;

import com.company.precost.calc.CalcConstants;

import java.math.BigDecimal;

/**
 * 영역 C - 가중평균 사용단가 V.
 * <pre>
 * V = (R*1000*S + T*1000*U) / (R*1000 + T*1000)
 *   R: 당월 입고수량(톤), S: 입고단가(원/kg)
 *   T: 기초재고(톤),     U: 기초재고 단가(원/kg)
 * </pre>
 * 경계: 분모(총 kg)가 0 이면 V = 0.
 */
public final class WeightedAvgPrice {

    private WeightedAvgPrice() {
    }

    public static BigDecimal compute(BigDecimal rTon, BigDecimal sPrice,
                                     BigDecimal tTon, BigDecimal uPrice) {
        BigDecimal rKg = nullToZero(rTon).multiply(CalcConstants.KG_PER_TON);
        BigDecimal tKg = nullToZero(tTon).multiply(CalcConstants.KG_PER_TON);
        BigDecimal totalKg = rKg.add(tKg);

        if (totalKg.signum() == 0) {
            return BigDecimal.ZERO.setScale(CalcConstants.PRICE_SCALE, CalcConstants.ROUNDING);
        }

        BigDecimal numerator = rKg.multiply(nullToZero(sPrice))
                .add(tKg.multiply(nullToZero(uPrice)));
        return numerator.divide(totalKg, CalcConstants.PRICE_SCALE, CalcConstants.ROUNDING);
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
