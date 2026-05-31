/* ============================================================
   전력비 계산 엔진 (엑셀 05.이동계획 산식 재현)
   - paper(제지/화장지): 생산량=kg, 원단위=kWh/ton(=사용량/생산량*1000), 전력비원단위=천원/ton(=원단위*단가/1000)
   - proc(가공/생리대/라미네이팅): 생산량=EA, 원단위=kWh/개(=사용량/생산량), 전력비원단위=원/개(=원단위*단가)
   - 검증: 제지3 1월 → 원단위 567.65, 전력비원단위 103.55

   [전력사용량 세분화]
   - 전력사용량(kWh) = ① 모고객(usageMo, 설비/호기별) + ② ESS(usageEss, 충전식)
     · 모고객: A방식(가동시간 비례) — 시간당전력_모고객 = 실적 usageMo / 실적 hours, 예상 = ×예상hours
     · ESS(충전식): essGuarantee[호기] × (평일 + 토요일) × essEff(0.9)
       (※ 일자 days = 월별 입력 가능. days.weekday + days.sat 이 충전 가동일)
   - 실적/이동계획 모두 동일 산식으로 계산

   [전력요금 6항목 + 회계비용] (공장 전체 레벨, byMonth[m].fee)
   - ①모고객요금(mo) ②ESS요금(ess) ③SPC지급금(spc) ④DR정산금(dr) ⑤복합보일러차감비(boiler) ⑥삼성보상금(samsung)
   - 회계비용 = mo + ess + spc − dr − boiler − samsung   (SPC는 가산 +)
   - ※ 요금 산식(요금 = 사용량 × 단가 …) 자체는 추후 반영. 현재는 주입된 fee 값을 표시/집계만.
   ============================================================ */
'use strict';

var PowerCalc = (function () {

  // 한 호기 계산: usage(kWh), prod(생산량), price(원/kWh), type('paper'|'proc')
  function computeLine(usage, prod, price, type) {
    usage = +usage || 0;
    prod = +prod || 0;
    price = +price || 0;
    var unit = 0;      // 전력원단위 (paper: kWh/ton, proc: kWh/개)
    var costUnit = 0;  // 전력비원단위 (paper: 천원/ton, proc: 원/개)
    if (prod > 0) {
      if (type === 'paper') {
        unit = usage / prod * 1000;
        costUnit = unit * price / 1000;
      } else {
        unit = usage / prod;
        costUnit = unit * price;
      }
    }
    var cost = usage * price; // 전력비 총액 [원]
    return { usage: usage, prod: prod, price: price, type: type, unit: unit, costUnit: costUnit, cost: cost };
  }

  // 한 달 전체 호기 계산
  // monthRec: { priceAcct, lines:{ 호기:{usage,usageMo,usageEss,prod,hours} }, days:{weekday,sat,holiday}, fee:{mo,ess,spc,dr,boiler,samsung} }
  // overrides: { 호기: {prod?, hours?, usage?}, price?, days?:{weekday,sat,holiday}, fee?:{...} }  (시뮬레이션/이동계획 입력값)
  //
  // 전력사용량 = 모고객(A방식) + ESS(충전식)
  //   모고객(A방식): 시간당전력_모고객 = 실적 usageMo / 실적 hours → 예상 usageMo = ×예상 hours(입력)
  //     · 실적 hours가 없으면 A방식 계수를 못 구하므로 B방식 fallback(실적 usageMo 그대로)
  //   ESS(충전식): essGuarantee[호기] × (days.weekday + days.sat) × essEff
  function computeMonth(monthRec, lineList, lineType, overrides, opt) {
    overrides = overrides || {};
    opt = opt || {};
    var essGuarantee = opt.essGuarantee || {};   // { 호기: kWh }
    var essEff = (opt.essEff != null) ? +opt.essEff : 0.9;

    var price = (overrides.price != null) ? +overrides.price : monthRec.priceAcct;

    // 일자(평일/토요일/휴일) — override 우선
    var baseDays = monthRec.days || { weekday: 0, sat: 0, holiday: 0 };
    var ovDays = overrides.days || {};
    var days = {
      weekday: (ovDays.weekday != null) ? +ovDays.weekday : (+baseDays.weekday || 0),
      sat: (ovDays.sat != null) ? +ovDays.sat : (+baseDays.sat || 0),
      holiday: (ovDays.holiday != null) ? +ovDays.holiday : (+baseDays.holiday || 0)
    };
    var chargeDays = days.weekday + days.sat;  // ESS 충전 가동일

    var rows = lineList.map(function (ln) {
      var base = monthRec.lines[ln] || { usage: 0, usageMo: 0, usageEss: 0, prod: 0, hours: null };
      var ov = overrides[ln] || {};
      var baseHours = (base.hours != null) ? +base.hours : null;
      var prod = (ov.prod != null) ? +ov.prod : base.prod;

      // 예상 가동시간: 입력값 있으면 그 값, 없으면 실적 가동시간
      var planHours = (ov.hours != null) ? +ov.hours : baseHours;

      var baseUsageMo = (base.usageMo != null) ? +base.usageMo : (+base.usage || 0);
      var baseUsageEss = (base.usageEss != null) ? +base.usageEss : 0;

      // ① 모고객 사용량 결정 (A방식)
      var usageMo, usageMode;
      if (ov.usage != null) {                              // 직접 usage override(예외 케이스): 전체를 모고객 취급
        usageMo = +ov.usage; usageMode = 'direct';
      } else if (baseHours != null && baseHours > 0) {     // A방식: 가동시간 비례
        var perHour = baseUsageMo / baseHours;             // 시간당 모고객 전력 (실적 계수)
        usageMo = perHour * (planHours != null ? planHours : baseHours);
        usageMode = 'A';
      } else {                                             // B방식 fallback: 실적 모고객 사용량 그대로
        usageMo = baseUsageMo; usageMode = 'B';
      }

      // ② ESS 사용량 (충전식): essGuarantee × (평일+토) × essEff
      var guarantee = +essGuarantee[ln] || 0;
      var usageEss, essMode;
      if (guarantee > 0 && chargeDays > 0) {
        usageEss = guarantee * chargeDays * essEff;
        essMode = 'charge';
      } else {
        usageEss = baseUsageEss;                           // 보증량/일자 없으면 실적 ESS 사용량 fallback
        essMode = 'B';
      }

      // 총 사용량 = 모고객 + ESS
      var usage = usageMo + usageEss;

      var r = computeLine(usage, prod, price, lineType[ln]);
      r.line = ln;
      r.baseProd = base.prod;
      r.baseUsage = (base.usage != null) ? +base.usage : (baseUsageMo + baseUsageEss);
      r.baseUsageMo = baseUsageMo;
      r.baseUsageEss = baseUsageEss;
      r.baseHours = baseHours;
      r.planHours = planHours;
      r.usageMode = usageMode;
      r.essMode = essMode;
      r.usageMo = usageMo;
      r.usageEss = usageEss;
      r.essGuarantee = guarantee;
      return r;
    });

    // 합계: 전력비 총액, 사용량(모고객/ESS/합계)
    var totals = rows.reduce(function (s, r) {
      s.usage += r.usage; s.usageMo += r.usageMo; s.usageEss += r.usageEss; s.cost += r.cost;
      return s;
    }, { usage: 0, usageMo: 0, usageEss: 0, cost: 0 });

    // 제지/화장지(paper) 군의 톤당 전력비 (가중)
    var paperRows = rows.filter(function (r) { return r.type === 'paper'; });
    var paperProd = paperRows.reduce(function (s, r) { return s + r.prod; }, 0);
    var paperCost = paperRows.reduce(function (s, r) { return s + r.cost; }, 0);
    totals.priceAcct = price;
    totals.paperProdKg = paperProd;
    totals.paperCost = paperCost;
    totals.paperCostPerTon = paperProd > 0 ? (paperCost / (paperProd / 1000)) : 0; // 원/ton

    // 요금 6항목 + 회계비용 (공장 전체 레벨, fee override 우선)
    var baseFee = monthRec.fee || {};
    var ovFee = overrides.fee || {};
    function pick(k) { return (ovFee[k] != null) ? +ovFee[k] : (+baseFee[k] || 0); }
    var fee = {
      mo: pick('mo'),
      ess: pick('ess'),
      spc: pick('spc'),
      dr: pick('dr'),
      boiler: pick('boiler'),
      samsung: pick('samsung')
    };
    // 회계비용 = ①모고객 + ②ESS + ③SPC − ④DR − ⑤복합보일러차감비 − ⑥삼성보상금
    fee.acct = fee.mo + fee.ess + fee.spc - fee.dr - fee.boiler - fee.samsung;

    return { price: price, rows: rows, totals: totals, days: days, fee: fee };
  }

  return { computeLine: computeLine, computeMonth: computeMonth };
})();

/* 숫자 포맷 */
var pfmt = {
  int: function (n) { return (Math.round(+n || 0)).toLocaleString('en-US'); },
  dec: function (n, d) { n = +n || 0; return n.toLocaleString('en-US', { minimumFractionDigits: d, maximumFractionDigits: d }); },
  won: function (n) { return (Math.round(+n || 0)).toLocaleString('en-US'); },
  eok: function (n) { return ((+n || 0) / 100000000).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); },
  baek: function (n) { return ((+n || 0) / 1000000).toLocaleString('en-US', { minimumFractionDigits: 1, maximumFractionDigits: 1 }); }
};
