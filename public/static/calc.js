/* 사전원가 시뮬레이션 - 산식 엔진 (module-calc JS 포팅) */
/* V:가중평균단가 P:사용량 W:재료비 X:톤당원가 Y:물량차이 Z:가격차이 AA:총차이 */
var Calc = (function () {
  var MILLION = 1000000;
  function weightedAvgPrice(R, S, T, U) {
    var denom = R + T;
    if (denom === 0) return 0;
    return (R * S + T * U) / denom;
  }
  function usage(Q, denomTon) { return Q * denomTon; }
  function materialCost(P, V) { return (P * V) / MILLION; }
  function costPerTon(W, X3) { return X3 === 0 ? 0 : (W / X3) * MILLION; }
  function volumeVariance(L, Q, M, X3) { return (L - Q) * M * X3; }
  function priceVariance(Q, M, V, X3) { return Q * (M - V) * X3; }
  function totalVariance(Y, Z) { return Y + Z; }
  function computeMaterial(opts) {
    var L = opts.L, M = opts.M, Q = opts.Q, R = opts.R, S = opts.S, T = opts.T, U = opts.U, X3 = opts.X3, denomTon = opts.denomTon;
    if (Q == null) Q = L;
    if (denomTon == null) denomTon = X3;
    var V = weightedAvgPrice(R || 0, S || 0, T || 0, U || 0) || M;
    var P = usage(Q, denomTon);
    var W = materialCost(P, V);
    var X = costPerTon(W, X3);
    var Y = volumeVariance(L, Q, M, X3);
    var Z = priceVariance(Q, M, V, X3);
    var AA = totalVariance(Y, Z);
    return { V: V, P: P, W: W, X: X, Y: Y, Z: Z, AA: AA };
  }
  return { weightedAvgPrice: weightedAvgPrice, usage: usage, materialCost: materialCost,
           costPerTon: costPerTon, volumeVariance: volumeVariance, priceVariance: priceVariance,
           totalVariance: totalVariance, computeMaterial: computeMaterial, MILLION: MILLION };
})();

var fmt = {
  won: function (n) { return Math.round(n).toLocaleString('ko-KR'); },
  wonSign: function (n) { return (n >= 0 ? '+' : '-') + Math.abs(Math.round(n)).toLocaleString('ko-KR'); },
  dec: function (n, d) { d = d == null ? 2 : d; return Number(n).toLocaleString('ko-KR', { minimumFractionDigits: d, maximumFractionDigits: d }); },
  ton: function (n) { return Number(n).toLocaleString('ko-KR'); },
  pct: function (n, d) { d = d == null ? 1 : d; return Number(n).toFixed(d) + '%'; },
  pctSign: function (n, d) { d = d == null ? 1 : d; return (n >= 0 ? '+' : '-') + Math.abs(n).toFixed(d) + '%'; }
};
