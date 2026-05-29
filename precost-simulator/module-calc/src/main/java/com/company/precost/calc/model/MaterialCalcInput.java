package com.company.precost.calc.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 자재 1건의 사전원가 계산 입력값.
 * 영역 A(전월 실적) + 영역 B(당월 계획)를 모은 값 객체.
 */
@Getter
@Builder
public class MaterialCalcInput {

    /** 자재코드 (분모톤 결정에 사용) */
    private final String materialCode;

    // --- 영역 A: 전월 실적 ---
    /** L: 전월 원단위(kg/Ton) */
    private final BigDecimal lPrevUnit;
    /** M: 전월 단가(원/kg) */
    private final BigDecimal mPrevPrice;

    // --- 영역 B: 당월 계획 ---
    /** Q: 당월 목표 원단위(kg/Ton). 미입력 시 L 사용 권장 */
    private final BigDecimal qCurrUnit;
    /** R: 당월 입고수량(톤) */
    private final BigDecimal rInboundTon;
    /** S: 당월 입고단가(원/kg) */
    private final BigDecimal sInboundPrice;
    /** T: 기초재고(톤) */
    private final BigDecimal tOpeningTon;
    /** U: 기초재고 단가(원/kg) */
    private final BigDecimal uOpeningPrice;
}
