package com.company.precost.calc.formula;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 영역 D 변동분석 산식 검증.
 *
 * <p>핵심 검증 케이스: PM2 / SC / 평량 220 / 자재 2000041 LATEX(금호 KSL 252)
 * <ul>
 *   <li>L=5.3643, M=1512.90, X3=16000, Q=5.20, V=1550</li>
 * </ul>
 *
 * <p><b>[검산 메모]</b> 요청서 문서의 기대값은 Y=+3,977,498 이나,
 * 주어진 입력(L=5.3643, M=1512.90)으로 수학적으로 정확히 계산하면 Y=3,977,112 이다(약 386원 차이).
 * (Y = (5.3643-5.20) * 1512.90 * 16000 = 3,977,111.52)
 * Y=3,977,498 이 되려면 M≈1513.047 이어야 하므로, 문서의 M(1512.90) 표기가
 * 실데이터(usage_2604.xlsx)의 소수점 정밀도와 다를 가능성이 높다.
 * Z=-3,086,720 은 문서값과 정확히 일치한다.
 * → 본 테스트는 산식의 수학적 정합성을 우선 검증하고,
 *   실데이터 확정 시 기대값을 재확인한다(아래 TODO 참조).
 */
@DisplayName("변동분석 산식 (영역 D) 검증")
class VarianceFormulaTest {

    private static final BigDecimal L = new BigDecimal("5.3643");
    private static final BigDecimal M = new BigDecimal("1512.90");
    private static final BigDecimal X3 = new BigDecimal("16000");
    private static final BigDecimal Q = new BigDecimal("5.20");
    private static final BigDecimal V = new BigDecimal("1550");

    @Nested
    @DisplayName("PM2 SC 220 LATEX 금호 KSL 252 - 5월 변동분석")
    class KosilLatexCase {

        @Test
        @DisplayName("Y(사용량차이) = (L-Q)*M*X3, 수학적 정확값 검증")
        void volumeVariance() {
            BigDecimal y = VolumeVariance.compute(L, Q, M, X3);
            // (5.3643 - 5.20) * 1512.90 * 16000 = 3,977,111.52 → 3,977,112
            assertThat(y.setScale(0, RoundingMode.HALF_UP))
                    .isEqualByComparingTo("3977112");
            // TODO[실데이터 확정]: 문서 기대값 3,977,498 (M 정밀도 재확인 필요)
        }

        @Test
        @DisplayName("Z(단가차이) = Q*(M-V)*X3, 문서 기대값과 일치")
        void priceVariance() {
            BigDecimal z = PriceVariance.compute(Q, M, V, X3);
            // 5.20 * (1512.90 - 1550) * 16000 = -3,086,720
            assertThat(z.setScale(0, RoundingMode.HALF_UP))
                    .isEqualByComparingTo("-3086720");
        }

        @Test
        @DisplayName("AA(재료비종합) = Y + Z")
        void totalVariance() {
            BigDecimal y = VolumeVariance.compute(L, Q, M, X3);
            BigDecimal z = PriceVariance.compute(Q, M, V, X3);
            BigDecimal aa = TotalVariance.sum(y, z);
            // 3,977,111.52 + (-3,086,720) = 890,391.52 → 890,392
            assertThat(aa.setScale(0, RoundingMode.HALF_UP))
                    .isEqualByComparingTo("890392");
            // TODO[실데이터 확정]: 문서 기대값 890,778
        }
    }

    @Nested
    @DisplayName("변동분석 부호 의미 검증")
    class SignMeaning {

        @Test
        @DisplayName("원단위 개선(L>Q)이면 Y>0 (개선)")
        void volumeImprovement() {
            BigDecimal y = VolumeVariance.compute(
                    new BigDecimal("6.0"), new BigDecimal("5.0"),
                    new BigDecimal("1000"), new BigDecimal("100"));
            assertThat(y.signum()).isPositive();
        }

        @Test
        @DisplayName("단가 상승(V>M)이면 Z<0 (악화)")
        void priceWorsening() {
            BigDecimal z = PriceVariance.compute(
                    new BigDecimal("5.0"), new BigDecimal("1000"),
                    new BigDecimal("1100"), new BigDecimal("100"));
            assertThat(z.signum()).isNegative();
        }

        @Test
        @DisplayName("null 입력은 0 으로 처리")
        void nullSafe() {
            assertThat(VolumeVariance.compute(null, Q, M, X3)).isEqualByComparingTo("0");
            assertThat(PriceVariance.compute(Q, null, V, X3)).isEqualByComparingTo("0");
            assertThat(TotalVariance.sum(null, null)).isEqualByComparingTo("0");
        }
    }
}
