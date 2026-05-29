package com.company.precost.calc;

import com.company.precost.calc.formula.CostPerTon;
import com.company.precost.calc.formula.DenominatorResolver;
import com.company.precost.calc.formula.MaterialCost;
import com.company.precost.calc.formula.MonthlyUsage;
import com.company.precost.calc.formula.PriceVariance;
import com.company.precost.calc.formula.TotalVariance;
import com.company.precost.calc.formula.VolumeVariance;
import com.company.precost.calc.formula.WeightedAvgPrice;
import com.company.precost.calc.model.DenominatorInput;
import com.company.precost.calc.model.MaterialCalcInput;
import com.company.precost.calc.model.MaterialCalcResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 사전원가 시뮬레이션 진입점.
 * 영역 A~D 산식을 조합하여 자재 1건의 계산 결과를 산출한다.
 * <p>순수 Java(무상태)로 구현되어 cost-simulation 모듈에서 자유롭게 호출한다.
 */
public final class CalcEngine {

    private CalcEngine() {
    }

    /**
     * 자재 1건 계산.
     *
     * @param input  전월 실적 + 당월 계획
     * @param denom  분모톤 결정 정보 (생산계획 톤)
     * @param x3Total X3: 당월 생산량(톤) - 톤당비용/변동분석 계산용
     */
    public static MaterialCalcResult compute(MaterialCalcInput input,
                                             DenominatorInput denom,
                                             BigDecimal x3Total) {
        if (input == null) {
            throw new CalcException("MaterialCalcInput 이 null 입니다.");
        }
        // Q 미입력 시 L 을 기본값으로 사용
        BigDecimal q = input.getQCurrUnit() != null ? input.getQCurrUnit() : input.getLPrevUnit();
        BigDecimal l = input.getLPrevUnit();
        BigDecimal m = input.getMPrevPrice();

        // 영역 C
        BigDecimal v = WeightedAvgPrice.compute(
                input.getRInboundTon(), input.getSInboundPrice(),
                input.getTOpeningTon(), input.getUOpeningPrice());
        BigDecimal denominatorTon = DenominatorResolver.resolve(input.getMaterialCode(), denom);
        BigDecimal p = MonthlyUsage.compute(q, denominatorTon);
        BigDecimal w = MaterialCost.compute(p, v);
        BigDecimal x = CostPerTon.compute(w, x3Total);

        // 영역 D
        BigDecimal y = VolumeVariance.compute(l, q, m, x3Total);
        BigDecimal z = PriceVariance.compute(q, m, v, x3Total);
        BigDecimal aa = TotalVariance.sum(y, z);

        return MaterialCalcResult.builder()
                .materialCode(input.getMaterialCode())
                .lPrevUnit(l)
                .mPrevPrice(m)
                .qCurrUnit(q)
                .vCurrPrice(v)
                .pUsageKg(p)
                .wCostMillion(w)
                .xCostPerTon(x)
                .yVolumeVariance(round0(y))
                .zPriceVariance(round0(z))
                .aaTotal(round0(aa))
                .build();
    }

    /** 금액(원)은 정수 반올림 */
    private static BigDecimal round0(BigDecimal v) {
        return v == null ? null : v.setScale(CalcConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
