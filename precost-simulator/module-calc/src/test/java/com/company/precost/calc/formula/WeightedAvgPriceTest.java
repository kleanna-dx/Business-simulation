package com.company.precost.calc.formula;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("가중평균 단가 V (영역 C) 검증")
class WeightedAvgPriceTest {

    @Test
    @DisplayName("정상 케이스: R=100,S=900,T=200,U=850 -> V=866.6667")
    void normal() {
        BigDecimal v = WeightedAvgPrice.compute(
                new BigDecimal("100"), new BigDecimal("900"),
                new BigDecimal("200"), new BigDecimal("850"));
        // (100*1000*900 + 200*1000*850) / (100*1000 + 200*1000)
        // = (90,000,000 + 170,000,000) / 300,000 = 866.6667
        assertThat(v).isEqualByComparingTo("866.6667");
    }

    @Test
    @DisplayName("경계: R=0 이면 V=U (기초재고 단가)")
    void inboundZero() {
        BigDecimal v = WeightedAvgPrice.compute(
                BigDecimal.ZERO, new BigDecimal("900"),
                new BigDecimal("200"), new BigDecimal("850"));
        assertThat(v).isEqualByComparingTo("850.0000");
    }

    @Test
    @DisplayName("경계: T=0 이면 V=S (입고단가)")
    void openingZero() {
        BigDecimal v = WeightedAvgPrice.compute(
                new BigDecimal("100"), new BigDecimal("900"),
                BigDecimal.ZERO, new BigDecimal("850"));
        assertThat(v).isEqualByComparingTo("900.0000");
    }

    @Test
    @DisplayName("경계: R=0 AND T=0 이면 V=0 (분모 0 방지)")
    void bothZero() {
        BigDecimal v = WeightedAvgPrice.compute(
                BigDecimal.ZERO, new BigDecimal("900"),
                BigDecimal.ZERO, new BigDecimal("850"));
        assertThat(v).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("null 입력은 0 으로 처리")
    void nullSafe() {
        BigDecimal v = WeightedAvgPrice.compute(null, null, null, null);
        assertThat(v).isEqualByComparingTo("0.0000");
    }
}
