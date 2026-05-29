package com.company.precost.calc;

import com.company.precost.calc.model.DenominatorInput;
import com.company.precost.calc.model.MaterialCalcInput;
import com.company.precost.calc.model.MaterialCalcResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CalcEngine 통합 시뮬레이션 검증")
class CalcEngineTest {

    @Test
    @DisplayName("PM2 SC 220 LATEX - 영역 C+D 통합 계산")
    void fullCompute() {
        MaterialCalcInput input = MaterialCalcInput.builder()
                .materialCode("1300041")   // ACB류 가정
                .lPrevUnit(new BigDecimal("5.3643"))
                .mPrevPrice(new BigDecimal("1512.90"))
                .qCurrUnit(new BigDecimal("5.20"))
                // V=1550 이 나오도록: R=100,S=1550,T=0,U=0
                .rInboundTon(new BigDecimal("100"))
                .sInboundPrice(new BigDecimal("1550"))
                .tOpeningTon(BigDecimal.ZERO)
                .uOpeningPrice(BigDecimal.ZERO)
                .build();

        DenominatorInput denom = DenominatorInput.builder()
                .x3Total(new BigDecimal("16000"))
                .acbTon(new BigDecimal("16000"))   // 단순화: 분모=X3
                .build();

        MaterialCalcResult r = CalcEngine.compute(input, denom, new BigDecimal("16000"));

        // V = 1550 (T=0 이므로 V=S)
        assertThat(r.getVCurrPrice()).isEqualByComparingTo("1550.0000");
        // Z = 5.20 * (1512.90 - 1550) * 16000 = -3,086,720
        assertThat(r.getZPriceVariance()).isEqualByComparingTo("-3086720");
        // Y = (5.3643 - 5.20) * 1512.90 * 16000 = 3,977,112
        assertThat(r.getYVolumeVariance()).isEqualByComparingTo("3977112");
        // AA = Y + Z = 890,392
        assertThat(r.getAaTotal()).isEqualByComparingTo("890392");
    }

    @Test
    @DisplayName("Q 미입력 시 L 을 기본 원단위로 사용 -> Y=0")
    void qDefaultsToL() {
        MaterialCalcInput input = MaterialCalcInput.builder()
                .materialCode("1100016")
                .lPrevUnit(new BigDecimal("5.0"))
                .mPrevPrice(new BigDecimal("1000"))
                .qCurrUnit(null)   // 미입력
                .rInboundTon(new BigDecimal("10"))
                .sInboundPrice(new BigDecimal("1000"))
                .tOpeningTon(BigDecimal.ZERO)
                .uOpeningPrice(BigDecimal.ZERO)
                .build();

        DenominatorInput denom = DenominatorInput.builder()
                .x3Total(new BigDecimal("1000"))
                .build();

        MaterialCalcResult r = CalcEngine.compute(input, denom, new BigDecimal("1000"));

        // Q=L 이므로 사용량차이 Y=0
        assertThat(r.getYVolumeVariance()).isEqualByComparingTo("0");
        assertThat(r.getQCurrUnit()).isEqualByComparingTo("5.0");
    }

    @Test
    @DisplayName("재료비 W, 톤당비용 X 계산 검증")
    void costCompute() {
        MaterialCalcInput input = MaterialCalcInput.builder()
                .materialCode("1100016")
                .lPrevUnit(new BigDecimal("5.0"))
                .mPrevPrice(new BigDecimal("1000"))
                .qCurrUnit(new BigDecimal("5.0"))
                .rInboundTon(new BigDecimal("100"))
                .sInboundPrice(new BigDecimal("1000"))
                .tOpeningTon(BigDecimal.ZERO)
                .uOpeningPrice(BigDecimal.ZERO)
                .build();

        DenominatorInput denom = DenominatorInput.builder()
                .x3Total(new BigDecimal("1000"))
                .build();

        MaterialCalcResult r = CalcEngine.compute(input, denom, new BigDecimal("1000"));

        // P = Q * 분모톤 = 5.0 * 1000 = 5000 kg
        assertThat(r.getPUsageKg()).isEqualByComparingTo("5000.0");
        // W = P * V / 1,000,000 = 5000 * 1000 / 1,000,000 = 5.0 (백만원)
        assertThat(r.getWCostMillion()).isEqualByComparingTo("5.0000");
        // X = W / X3 * 1,000,000 = 5.0 / 1000 * 1,000,000 = 5000 원/톤
        assertThat(r.getXCostPerTon().setScale(0, RoundingMode.HALF_UP))
                .isEqualByComparingTo("5000");
    }
}
