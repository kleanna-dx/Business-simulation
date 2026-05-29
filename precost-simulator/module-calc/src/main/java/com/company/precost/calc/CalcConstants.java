package com.company.precost.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 사전원가 계산 공통 상수.
 */
public final class CalcConstants {

    private CalcConstants() {
    }

    /** 톤 → kg 환산 (1톤 = 1000kg) */
    public static final BigDecimal KG_PER_TON = BigDecimal.valueOf(1000);

    /** 백만원 환산 (재료비 W = P*V / 1,000,000) */
    public static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    /** 단가 계산 스케일 (원/kg, 소수 4자리) */
    public static final int PRICE_SCALE = 4;

    /** 원단위 스케일 (kg/Ton, 소수 6자리) */
    public static final int UNIT_SCALE = 6;

    /** 금액 스케일 (원, 정수) */
    public static final int MONEY_SCALE = 0;

    /** 모든 반올림은 HALF_UP (사내 표준) */
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
}
