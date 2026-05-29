/* ============================================================
   전력비 계산 엔진 (엑셀 05.이동계획 산식 재현)
   - paper(제지/화장지): 생산량=kg, 원단위=kWh/ton(=사용량/생산량*1000), 전력비원단위=천원/ton(=원단위*단가/1000)
   - proc(가공/생리대/라미네이팅): 생산량=EA, 원단위=kWh/개(=사용량/생산량), 전력비원단위=원/개(=원단위*단가)
   - 검증: 제지3 1월 → 원단위 567.65, 전력비원단위 103.55
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
  // month: { priceAcct, lines:{ 호기:{usage,prod} } }, lineList: [...], lineType:{호기:type}
  // overrides: { 호기: {prod?, usage?}, price? }  (시뮬레이션 입력값)
  function computeMonth(monthRec, lineList, lineType, overrides) {
    overrides = overrides || {};
    var price = (overrides.price != null) ? +overrides.price : monthRec.priceAcct;
    var rows = lineList.map(function (ln) {
      var base = monthRec.lines[ln] || { usage: 0, prod: 0 };
      var ov = overrides[ln] || {};
      var usage = (ov.usage != null) ? +ov.usage : base.usage;
      var prod = (ov.prod != null) ? +ov.prod : base.prod;
      var r = computeLine(usage, prod, price, lineType[ln]);
      r.line = ln;
      r.baseProd = base.prod;
      r.baseUsage = base.usage;
      return r;
    });
    // 합계: 전력비 총액, 사용량 (생산량은 단위가 섞여 합산 의미 제한적)
    var totals = rows.reduce(function (s, r) {
      s.usage += r.usage; s.cost += r.cost;
      return s;
    }, { usage: 0, cost: 0 });
    // 제지/화장지(paper) 군의 톤당 전력비 (가중)
    var paperRows = rows.filter(function (r) { return r.type === 'paper'; });
    var paperProd = paperRows.reduce(function (s, r) { return s + r.prod; }, 0);
    var paperCost = paperRows.reduce(function (s, r) { return s + r.cost; }, 0);
    totals.priceAcct = price;
    totals.paperProdKg = paperProd;
    totals.paperCost = paperCost;
    totals.paperCostPerTon = paperProd > 0 ? (paperCost / (paperProd / 1000)) : 0; // 원/ton
    return { price: price, rows: rows, totals: totals };
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
