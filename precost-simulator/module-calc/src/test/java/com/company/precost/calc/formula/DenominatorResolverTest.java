package com.company.precost.calc.formula;

import com.company.precost.calc.CalcException;
import com.company.precost.calc.model.DenominatorInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("분모톤 결정 (영역 C) 검증")
class DenominatorResolverTest {

    private final DenominatorInput input = DenominatorInput.builder()
            .x3Total(new BigDecimal("16000"))
            .jigwanTon(new BigDecimal("500"))
            .ukpTon(new BigDecimal("300"))
            .kbTon(new BigDecimal("4000"))
            .acbTon(new BigDecimal("2000"))
            .build();

    @Test
    @DisplayName("펄프(11xxxxx) -> X3 전체")
    void pulp() {
        assertThat(DenominatorResolver.resolve("1100016", input))
                .isEqualByComparingTo("16000");
    }

    @Test
    @DisplayName("일반 고지(12xxxxx) -> X3 - 지관 - UKP")
    void wastePaper() {
        // 16000 - 500 - 300 = 15200
        assertThat(DenominatorResolver.resolve("1200000", input))
                .isEqualByComparingTo("15200");
    }

    @Test
    @DisplayName("HINTON UKP(1100023) -> KB 톤 (펄프 prefix 보다 우선)")
    void hintonUkp() {
        assertThat(DenominatorResolver.resolve("1100023", input))
                .isEqualByComparingTo("4000");
    }

    @Test
    @DisplayName("UKP(1100005) -> KB 톤")
    void ukp() {
        assertThat(DenominatorResolver.resolve("1100005", input))
                .isEqualByComparingTo("4000");
    }

    @Test
    @DisplayName("ACB류(13xxxxx) -> ACB 톤")
    void acb() {
        assertThat(DenominatorResolver.resolve("1300001", input))
                .isEqualByComparingTo("2000");
    }

    @Test
    @DisplayName("빈 자재코드 -> CalcException")
    void blankCode() {
        assertThatThrownBy(() -> DenominatorResolver.resolve("", input))
                .isInstanceOf(CalcException.class);
        assertThatThrownBy(() -> DenominatorResolver.resolve(null, input))
                .isInstanceOf(CalcException.class);
    }

    @Test
    @DisplayName("input 이 null 이면 CalcException")
    void nullInput() {
        assertThatThrownBy(() -> DenominatorResolver.resolve("1100016", null))
                .isInstanceOf(CalcException.class);
    }
}
