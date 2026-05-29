package com.company.precost.calc.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 자재 1건의 사전원가 계산 결과 (영역 C, D).
 */
@Getter
@Builder
public class MaterialCalcResult {

    private final String materialCode;

    // 입력 echo (결과표 표시용)
    private final BigDecimal lPrevUnit;   // L
    private final BigDecimal mPrevPrice;  // M
    private final BigDecimal qCurrUnit;   // Q

    // 영역 C
    private final BigDecimal vCurrPrice;     // V 가중평균 단가
    private final BigDecimal pUsageKg;       // P 사용량(kg)
    private final BigDecimal wCostMillion;   // W 재료비(백만원)
    private final BigDecimal xCostPerTon;    // X 톤당비용(원/톤)

    // 영역 D
    private final BigDecimal yVolumeVariance; // Y 사용량차이(원)
    private final BigDecimal zPriceVariance;  // Z 단가차이(원)
    private final BigDecimal aaTotal;         // AA 재료비종합(원)
}
