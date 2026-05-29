package com.company.precost.calc.formula;

import com.company.precost.calc.CalcException;
import com.company.precost.calc.model.DenominatorInput;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 영역 C - 자재별 분모톤 결정.
 * <pre>
 * 펄프(11xxxxx)        → X3 전체
 * 일반 고지(12xxxxx)   → X3 - 지관 - UKP
 * UKP/HINTON UKP       → KB 톤 (1100005, 1100023)
 * ACB류                → ACB 톤
 * </pre>
 */
public final class DenominatorResolver {

    /** UKP / HINTON UKP 자재코드 (KB 톤을 분모로 사용) */
    private static final Set<String> UKP_CODES = Set.of("1100005", "1100023");

    private DenominatorResolver() {
    }

    public static BigDecimal resolve(String materialCode, DenominatorInput input) {
        if (materialCode == null || materialCode.isBlank()) {
            throw new CalcException("자재코드가 비어 있습니다.");
        }
        if (input == null) {
            throw new CalcException("분모톤 입력(DenominatorInput)이 null 입니다.");
        }

        // 1순위: UKP / HINTON UKP → KB 톤
        if (UKP_CODES.contains(materialCode)) {
            return input.kbTon();
        }

        // 펄프 (11xxxxx) → X3 전체
        if (materialCode.startsWith("11")) {
            return input.x3Total();
        }

        // 일반 고지 (12xxxxx) → X3 - 지관 - UKP
        if (materialCode.startsWith("12")) {
            return input.x3Total()
                    .subtract(input.jigwanTon())
                    .subtract(input.ukpTon());
        }

        // ACB류 (예: 13xxxxx 로 매핑 가정) → ACB 톤
        if (materialCode.startsWith("13")) {
            return input.acbTon();
        }

        // 그 외 자재 → X3 전체 (기본값)
        return input.x3Total();
    }
}
