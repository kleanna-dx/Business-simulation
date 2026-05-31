/* ============================================================
   사전원가 시뮬레이션 웹 시스템 — SPA (vanilla JS)
   ============================================================ */
'use strict';

var NAV = [
  { g: '운영' },
  { id: 'dashboard', icon: 'fa-gauge-high', label: '대시보드' },
  { g: 'SAP · 기준정보' },
  { id: 'sapsync', icon: 'fa-rotate', label: 'SAP BW 수신', star: true },
  { id: 'master', icon: 'fa-layer-group', label: '자재 마스터' },
  { id: 'prodplan', icon: 'fa-industry', label: '예상 생산량 기준정보', star: true },
  { g: '계획 · 시뮬레이션' },
  { id: 'plan', icon: 'fa-clipboard-list', label: '생산·원가 계획' },
  { id: 'movement', icon: 'fa-truck-fast', label: '이동 계획', star: true },
  { id: 'actual', icon: 'fa-table-cells', label: '실적 입력' },
  { id: 'sim', icon: 'fa-sliders', label: '사전원가 시뮬레이션', star: true },
  { id: 'power', icon: 'fa-bolt', label: '전력비 시뮬레이션', star: true },
  { id: 'ai', icon: 'fa-robot', label: 'AI 분석 어시스턴트', star: true },
  { g: '승인' },
  { id: 'approval', icon: 'fa-circle-check', label: '승인 워크플로', badge: 2 }
];

var TITLES = {
  dashboard: ['대시보드', '5월 사전원가 현황 및 핵심 지표'],
  sapsync:   ['SAP BW 수신', 'SAP에서 자동 수신한 기준정보(BW) 확인'],
  master:    ['자재 마스터', '자재 기준정보 및 가중평균 단가 구성'],
  plan:      ['생산·원가 계획', '호기별 월 생산계획 및 원가 계획'],
  movement:  ['이동 계획', '입고·출고·이송·조정·출하·반품 통합 관리'],
  actual:    ['실적 입력', '월 실적 원단위 입력 및 검증'],
  sim:       ['사전원가 시뮬레이션', '슬라이더로 변수 조정 → 원가·차이 실시간 계산'],
  prodplan:  ['예상 생산량 기준정보', '호기·월별 예상 생산량 입력(또는 엑셀 업로드) → 전 시뮬레이션 공통 기준'],
  power:     ['전력비 시뮬레이션', '예상 생산량 기준정보 자동 반영 · 단가 조정 → 전력원단위·전력비 실시간 계산'],
  ai:        ['AI 분석 어시스턴트', '자연어로 원가 차이 원인 분석'],
  approval:  ['승인 워크플로', '사전원가 확정 결재 진행 현황']
};

var App = { state: { route: 'dashboard', simYear: null } };

/* ====================== 실적/예상 기준일(Cutoff) ======================
   현재일 기준으로 "전달까지"는 실적(SAP, 확정·수정 금지),
   "현재달부터"는 예상(이동계획, 입력·예측 적용).
   예) 2026-05-31 → 2026-01~04 실적, 2026-05~ 예상.
   - 데모는 2026년 데이터이므로, 실제 today가 2026이 아니면
     '?asof=YYYY-MM' 파라미터로 기준월을 덮어쓸 수 있다(데모/검증용). */
var Period = {
  // 기준월(현재월) 'YYYY-MM'. 이 달 포함 이후가 예상.
  cutoff: (function () {
    try {
      var q = new URLSearchParams(location.search);
      var asof = q.get('asof');
      if (asof && /^\d{4}-\d{1,2}$/.test(asof)) {
        var p = asof.split('-'); return p[0] + '-' + ('0' + p[1]).slice(-2);
      }
    } catch (e) {}
    var d = new Date();
    return d.getFullYear() + '-' + ('0' + (d.getMonth() + 1)).slice(-2);
  })(),
  // month < cutoff → 실적 확정(과거)
  isActual: function (month) { return !!month && month < this.cutoff; },
  // month >= cutoff → 예상(현재월 포함 미래)
  isForecast: function (month) { return !!month && month >= this.cutoff; },
  label: function (month) { return this.isActual(month) ? '실적 확정' : '예상(이동계획)'; }
};

function el(html) { var d = document.createElement('div'); d.innerHTML = html.trim(); return d.firstChild; }
function $(sel, root) { return (root || document).querySelector(sel); }
function $all(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

function renderShell() {
  // 승인 대기 건수: 데이터가 있을 때만 배지 표시 (운영 빈 상태에서는 0)
  var pendingCnt = DB.hasData()
    ? DB.approvals.filter(function (a) { return a.statusKey === 'SUBMITTED'; }).length
    : 0;
  var navHtml = NAV.map(function (n) {
    if (n.g) return '<div class="nav__group">' + n.g + '</div>';
    var badgeVal = (n.id === 'approval') ? pendingCnt : n.badge;
    var extra = badgeVal ? '<span class="badge">' + badgeVal + '</span>'
      : (n.star ? '<span class="star"><i class="fas fa-star"></i></span>' : '');
    return '<div class="nav__item" data-route="' + n.id + '">'
      + '<i class="fas ' + n.icon + '"></i><span>' + n.label + '</span>' + extra + '</div>';
  }).join('');

  var shell =
  '<div class="app">'
  + '<aside class="sidebar">'
  +   '<div class="sidebar__brand">'
  +     '<div class="sidebar__logo"><i class="fas fa-calculator"></i></div>'
  +     '<div><div class="sidebar__title">사전원가 시뮬레이션</div><div class="sidebar__sub">Pre-Cost Simulation</div></div>'
  +   '</div>'
  +   '<nav class="nav">' + navHtml + '</nav>'
  +   '<div class="sidebar__foot">v2.0 · 제지/위생용지 원가<br>' + DB.meta.plantNm + '</div>'
  + '</aside>'
  + '<div class="main">'
  +   '<header class="topbar">'
  +     '<div><div class="topbar__crumb" id="crumb">운영 / 대시보드</div><div class="topbar__title" id="ptitle">대시보드</div></div>'
  +     '<div class="topbar__spacer"></div>'
  +     '<div class="topbar__month"><i class="fas fa-calendar-days"></i>' + DB.meta.monthLabel + '</div>'
  +     '<div class="icon-btn"><i class="fas fa-bell"></i><span class="dot"></span></div>'
  +     '<div class="user-cell"><div class="avatar">원</div><div><div class="nm">' + DB.meta.user + '</div><div class="ro">' + DB.meta.roleNm + '</div></div></div>'
  +   '</header>'
  +   '<main id="page"></main>'
  + '</div>'
  + '</div>';

  document.getElementById('root').innerHTML = shell;
  $all('.nav__item').forEach(function (item) {
    item.addEventListener('click', function () { location.hash = item.getAttribute('data-route'); });
  });
}

function route() {
  var id = (location.hash || '#dashboard').replace('#', '');
  if (!PAGES[id]) id = 'dashboard';
  App.state.route = id;
  $all('.nav__item').forEach(function (it) {
    it.classList.toggle('active', it.getAttribute('data-route') === id);
  });
  var t = TITLES[id] || ['', ''];
  var grp = '운영';
  for (var i = 0; i < NAV.length; i++) { if (NAV[i].g) grp = NAV[i].g; if (NAV[i].id === id) break; }
  $('#crumb').textContent = grp + ' / ' + t[0];
  $('#ptitle').textContent = t[0];
  var head = '<div class="page__head"><div class="grow"><h1>' + t[0] + '</h1><p>' + t[1] + '</p></div></div>';
  $('#page').innerHTML = '<div class="page">' + head + PAGES[id]() + '</div>';
  window.scrollTo(0, 0);
  if (AFTER[id]) AFTER[id]();
}

/* ---------- shared helpers ---------- */
function kpiCard(label, val, unit, ico, icoCls, deltaCls, deltaIco, deltaTxt) {
  return '<div class="kpi"><div class="kpi__top"><span class="kpi__label">' + label + '</span>'
    + '<span class="kpi__ico ' + icoCls + '"><i class="fas ' + ico + '"></i></span></div>'
    + '<div class="kpi__val">' + val + (unit ? '<span class="unit">' + unit + '</span>' : '') + '</div>'
    + (deltaTxt ? '<div class="kpi__delta ' + deltaCls + '"><i class="fas ' + deltaIco + '"></i>' + deltaTxt + '</div>' : '')
    + '</div>';
}
function miniStat(label, val, cls) {
  return '<div class="mini-stat"><span class="ms-lab">' + label + '</span><span class="ms-val ' + (cls || '') + '">' + val + '</span></div>';
}
function tag(text, key) { return '<span class="tag ' + (TAGCLS[key] || 'tag-gray') + '">' + text + '</span>'; }
function alertRow(a) {
  var ic = a.sev === 'high' ? 'ico-red' : (a.sev === 'warn' ? 'ico-amber' : 'ico-blue');
  return '<div class="alert-item"><div class="ai-ico ' + ic + '"><i class="fas ' + a.icon + '"></i></div>'
    + '<div class="ai-body"><div class="ai-title">' + a.title + '</div><div class="ai-meta">' + a.meta + '</div></div></div>';
}

/* Empty State — 운영 빌드(데이터 없음)에서 각 화면에 표시 */
function emptyState(msg) {
  return '<div class="empty-state">'
    + '<div class="empty-state__ico"><i class="fas fa-database"></i></div>'
    + '<h3 class="empty-state__title">데이터 없음</h3>'
    + '<p class="empty-state__msg">' + (msg || '표시할 데이터가 없습니다.') + '</p>'
    + '<div class="empty-state__tag"><i class="fas fa-plug"></i> API 연동 대기 중</div>'
    + '<p class="empty-state__hint">운영 서버에서는 Spring Boot API(<code>/api/...</code>) 연동 후 데이터가 표시됩니다.</p>'
    + '</div>';
}

var PAGES = {};
var AFTER = {};

/* ====================== 대시보드 (slide 11) ====================== */
function top5Rows() {
  var rows = DB.materials.map(function (m) {
    var r = Calc.computeMaterial(Object.assign({}, m, { X3: DB.meta.productionTon }));
    return { nm: m.name, code: m.code, aa: r.AA };
  }).sort(function (a, b) { return Math.abs(b.aa) - Math.abs(a.aa); });
  var max = Math.max.apply(null, rows.map(function (r) { return Math.abs(r.aa); }));
  return rows.map(function (r) {
    var pos = r.aa >= 0;
    var w = max ? (Math.abs(r.aa) / max * 100) : 0;
    return '<div style="margin-bottom:13px">'
      + '<div style="display:flex;justify-content:space-between;margin-bottom:5px">'
      + '<span style="font-size:13px;font-weight:600">' + r.nm + ' <span class="cell-code">' + r.code + '</span></span>'
      + '<span style="font-size:13px;font-weight:700;color:' + (pos ? 'var(--down)' : 'var(--up)') + '">' + fmt.wonSign(r.aa) + '원</span></div>'
      + '<div class="progress"><i style="width:' + w + '%;background:' + (pos ? 'var(--down)' : 'var(--up)') + '"></i></div></div>';
  }).join('');
}

PAGES.dashboard = function () {
  if (!DB.hasData()) return emptyState('사전원가 현황 데이터가 아직 없습니다. SAP BW 수신 및 시뮬레이션 후 표시됩니다.');
  var totalCost = DB.trend.PM2[DB.trend.PM2.length - 1];
  var prevCost = DB.trend.PM2[DB.trend.PM2.length - 2];
  var deltaPct = ((totalCost - prevCost) / prevCost * 100);
  var totalVar = DB.materials.reduce(function (s, m) {
    return s + Calc.computeMaterial(Object.assign({}, m, { X3: DB.meta.productionTon })).AA;
  }, 0);
  var costPerTon = totalCost * 1000000 / DB.meta.productionTon;

  var kpis = '<div class="grid g-4" style="margin-bottom:16px">'
    + kpiCard('5월 사전원가 (PM2)', fmt.dec(totalCost / 1000, 2), '억원', 'fa-won-sign', 'ico-blue',
        deltaPct >= 0 ? 'delta-down' : 'delta-up', deltaPct >= 0 ? 'fa-arrow-up' : 'fa-arrow-down', fmt.pctSign(deltaPct) + ' 전월대비')
    + kpiCard('톤당 원가', fmt.won(costPerTon), '원/톤', 'fa-weight-hanging', 'ico-indigo', 'delta-flat', 'fa-minus', '생산 ' + fmt.ton(DB.meta.productionTon) + '톤')
    + kpiCard('총 원가차이', fmt.wonSign(totalVar / 1000000) + 'M', '원', 'fa-scale-unbalanced', 'ico-amber',
        totalVar >= 0 ? 'delta-down' : 'delta-up', totalVar >= 0 ? 'fa-arrow-up' : 'fa-arrow-down', '물량+가격 차이')
    + kpiCard('승인 대기', String(DB.approvals.filter(function (a) { return a.statusKey === 'SUBMITTED'; }).length), '건', 'fa-hourglass-half', 'ico-red', 'delta-flat', 'fa-clock', '마감 D-3')
    + '</div>';

  var trendCard = '<div class="card"><div class="card__head"><h3>월별 사전원가 추이</h3><span class="sub">단위: 백만원</span>'
    + '<div class="grow"></div><span class="legend"><span class="l"><span class="sw" style="background:var(--blue)"></span>PM2</span>'
    + '<span class="l"><span class="sw" style="background:var(--blue-400)"></span>PM3</span></span></div>'
    + '<div class="card__body"><div class="bars" id="trendBars"></div>'
    + '<div style="display:flex;justify-content:space-between;margin-top:8px">'
    + DB.trend.labels.map(function (l) { return '<span class="lab" style="flex:1;text-align:center;font-size:11px;color:var(--muted)">' + l + '</span>'; }).join('')
    + '</div></div></div>';

  var top5Card = '<div class="card"><div class="card__head"><h3>자재별 원가차이 TOP</h3><span class="sub">VAR 분석</span></div>'
    + '<div class="card__body">' + top5Rows() + '</div></div>';

  var moveCard = '<div class="card"><div class="card__head"><h3>이동 요약</h3><div class="grow"></div>'
    + '<a class="btn btn-sm" href="#movement">전체보기 <i class="fas fa-arrow-right"></i></a></div><div class="card__body">'
    + miniStat('입고', '2건 · 9,680 kg', '')
    + miniStat('출고', '1건 · 5,180 kg', '')
    + miniStat('이송 진행중', '1건', 'tag-cell')
    + miniStat('예정', '2건', '')
    + '</div></div>';

  var alertCard = '<div class="card"><div class="card__head"><h3>알림</h3><div class="grow"></div>'
    + '<span class="tag tag-down">' + DB.alerts.filter(function (a) { return a.sev === 'high'; }).length + ' 긴급</span></div>'
    + '<div class="card__body">' + DB.alerts.map(alertRow).join('') + '</div></div>';

  return kpis
    + '<div class="grid g-2" style="margin-bottom:16px">' + trendCard + top5Card + '</div>'
    + '<div class="grid g-2">' + moveCard + alertCard + '</div>';
};

AFTER.dashboard = function () {
  if (!DB.hasData()) return;
  var box = $('#trendBars'); if (!box) return;
  var all = DB.trend.PM2.concat(DB.trend.PM3);
  var max = Math.max.apply(null, all);
  box.innerHTML = DB.trend.labels.map(function (l, i) {
    var h1 = DB.trend.PM2[i] / max * 100, h2 = DB.trend.PM3[i] / max * 100;
    return '<div class="grp"><div class="stack">'
      + '<div class="bar a" style="height:' + h1 + '%" title="PM2 ' + DB.trend.PM2[i] + '"></div>'
      + '<div class="bar b" style="height:' + h2 + '%" title="PM3 ' + DB.trend.PM3[i] + '"></div>'
      + '</div></div>';
  }).join('');
};

/* ============== 사전원가 시뮬레이션 (slide 8) — PRIORITY ============== */
PAGES.sim = function () {
  if (!DB.hasData()) return emptyState('시뮬레이션할 자재 데이터가 없습니다. 자재 마스터 연동 후 이용할 수 있습니다.');
  var stepper = '<div class="stepper">'
    + '<div class="step done"><div class="step__dot"><i class="fas fa-check"></i></div><div class="step__lab">SAP BW 수신</div></div><div class="step__line"></div>'
    + '<div class="step done"><div class="step__dot"><i class="fas fa-check"></i></div><div class="step__lab">기준정보 확정</div></div><div class="step__line"></div>'
    + '<div class="step current"><div class="step__dot">3</div><div class="step__lab">시뮬레이션</div></div><div class="step__line"></div>'
    + '<div class="step"><div class="step__dot">4</div><div class="step__lab">승인 상신</div></div></div>';

  function slider(id, label, min, max, val, step, unit, scaleMin, scaleMax) {
    return '<div class="slider-row" data-sim="' + id + '">'
      + '<div class="slider-row__top"><span class="lab">' + label + '</span>'
      + '<span class="val"><span class="num" id="v_' + id + '">' + val + '</span><span class="u">' + unit + '</span></span></div>'
      + '<input type="range" id="s_' + id + '" min="' + min + '" max="' + max + '" step="' + step + '" value="' + val + '">'
      + '<div class="slider-row__scale"><span>' + scaleMin + '</span><span>' + scaleMax + '</span></div></div>';
  }

  var controls = '<div class="card"><div class="card__head"><h3><i class="fas fa-sliders" style="color:var(--blue);margin-right:7px"></i>시뮬레이션 변수</h3>'
    + '<div class="grow"></div>' + tag('USR 입력', 'IN_PROGRESS') + '</div><div class="card__body">'
    + slider('ton', '생산량', 8000, 24000, DB.meta.productionTon, 500, '톤', '8,000', '24,000')
    + slider('defect', '폐품율', 0, 5, DB.meta.defectRate, 0.1, '%', '0%', '5%')
    + slider('unit', '원단위 조정', -10, 10, 0, 0.5, '%', '-10%', '+10%')
    + slider('price', '단가 조정', -10, 10, 0, 0.5, '%', '-10%', '+10%')
    + '<hr class="hr"><div class="note"><i class="fas fa-circle-info"></i> 슬라이더를 움직이면 KPI·폭포차트·자재표가 실시간으로 재계산됩니다.</div>'
    + '<button class="btn btn-primary" style="width:100%;margin-top:14px" onclick="location.hash=\'approval\'"><i class="fas fa-paper-plane"></i> 시뮬레이션 결과 상신</button>'
    + '</div></div>';

  var kpis = '<div class="grid g-3" id="simKpis"></div>';
  var wf = '<div class="card"><div class="card__head"><h3>원가차이 폭포 분석</h3><span class="sub">기준 → 물량차이(Y) → 가격차이(Z) → 결과</span></div>'
    + '<div class="card__body"><div class="waterfall" id="simWaterfall"></div></div></div>';
  var tbl = '<div class="card"><div class="card__head"><h3>자재별 상세</h3><div class="grow"></div>'
    + '<span class="legend"><span class="l">' + tag('SAP', 'INBOUND') + '자동</span><span class="l">' + tag('SYS', 'TRANSFER') + '계산</span></span></div>'
    + '<div class="tbl-wrap" style="border:none;border-radius:0"><table class="tbl"><thead><tr>'
    + '<th>자재</th><th class="num">표준원단위 L</th><th class="num">실적원단위 Q</th><th class="num">가중단가 V</th>'
    + '<th class="num">재료비 W(M)</th><th class="num">물량차이 Y</th><th class="num">가격차이 Z</th><th class="num">총차이 AA</th>'
    + '</tr></thead><tbody id="simTbody"></tbody></table></div></div>';

  return stepper
    + '<div class="grid" style="grid-template-columns:340px 1fr;gap:16px;align-items:start">'
    + controls
    + '<div class="grid" style="gap:16px">' + kpis + wf + tbl + '</div>'
    + '</div>';
};

function simCompute() {
  var ton = +$('#s_ton').value;
  var defect = +$('#s_defect').value;
  var unitAdj = +$('#s_unit').value / 100;
  var priceAdj = +$('#s_price').value / 100;
  var effTon = ton * (1 + defect / 100);  // 폐품 보정 생산톤
  var rows = DB.materials.map(function (m) {
    var mm = Object.assign({}, m, {
      Q: m.Q * (1 + unitAdj),
      S: m.S * (1 + priceAdj),
      U: m.U * (1 + priceAdj),
      M: m.M,
      X3: effTon
    });
    var r = Calc.computeMaterial(mm);
    return { m: m, mm: mm, r: r };
  });
  var totW = rows.reduce(function (s, x) { return s + x.r.W; }, 0);
  var totY = rows.reduce(function (s, x) { return s + x.r.Y; }, 0);
  var totZ = rows.reduce(function (s, x) { return s + x.r.Z; }, 0);
  var totAA = totY + totZ;
  var perTon = totW * 1000000 / effTon;
  return { ton: ton, effTon: effTon, rows: rows, totW: totW, totY: totY, totZ: totZ, totAA: totAA, perTon: perTon };
}

function simRender() {
  var c = simCompute();
  // sliders value labels
  $('#v_ton').textContent = fmt.ton(+$('#s_ton').value);
  $('#v_defect').textContent = (+$('#s_defect').value).toFixed(1);
  $('#v_unit').textContent = (+$('#s_unit').value >= 0 ? '+' : '') + (+$('#s_unit').value).toFixed(1);
  $('#v_price').textContent = (+$('#s_price').value >= 0 ? '+' : '') + (+$('#s_price').value).toFixed(1);

  // KPIs
  $('#simKpis').innerHTML =
    kpiCard('총 재료비', fmt.dec(c.totW, 1), '백만원', 'fa-coins', 'ico-blue', 'delta-flat', 'fa-cube', DB.materials.length + '개 자재')
    + kpiCard('톤당 원가', fmt.won(c.perTon), '원/톤', 'fa-weight-hanging', 'ico-indigo', 'delta-flat', 'fa-arrows-down-to-line', '폐품보정 ' + fmt.ton(Math.round(c.effTon)) + '톤')
    + kpiCard('총 원가차이 (AA)', fmt.wonSign(c.totAA), '원', 'fa-scale-unbalanced', c.totAA >= 0 ? 'ico-red' : 'ico-green',
        c.totAA >= 0 ? 'delta-down' : 'delta-up', c.totAA >= 0 ? 'fa-arrow-up' : 'fa-arrow-down', c.totAA >= 0 ? '원가 증가' : '원가 절감');

  // waterfall: base(|Y|+|Z| scale) -> Y -> Z -> net
  var base = Math.abs(c.totY) + Math.abs(c.totZ);
  var scale = Math.max(base, Math.abs(c.totAA), 1);
  function bar(cls, label, amt, pct, color) {
    return '<div class="wf-col"><div class="wf-amt" style="color:' + color + '">' + fmt.wonSign(amt / 1000000) + 'M</div>'
      + '<div class="wf-bar ' + cls + '" style="height:' + Math.max(pct, 3) + '%"></div><div class="wf-lab">' + label + '</div></div>';
  }
  $('#simWaterfall').innerHTML =
    bar('base', '기준원가', c.totW * 1000000, 70, 'var(--muted)')
    + bar(c.totY >= 0 ? 'up' : 'down', '물량차이 Y', c.totY, Math.abs(c.totY) / scale * 100, c.totY >= 0 ? 'var(--down)' : 'var(--up)')
    + bar(c.totZ >= 0 ? 'up' : 'down', '가격차이 Z', c.totZ, Math.abs(c.totZ) / scale * 100, c.totZ >= 0 ? 'var(--down)' : 'var(--up)')
    + bar('net', '총차이 AA', c.totAA, Math.abs(c.totAA) / scale * 100, 'var(--blue)');

  // table
  $('#simTbody').innerHTML = c.rows.map(function (x) {
    var posAA = x.r.AA >= 0;
    return '<tr><td><b>' + x.m.name + '</b> <span class="cell-code">' + x.m.code + '</span></td>'
      + '<td class="num">' + fmt.dec(x.m.L, 4) + '</td>'
      + '<td class="num">' + fmt.dec(x.mm.Q, 4) + '</td>'
      + '<td class="num">' + fmt.won(x.r.V) + '</td>'
      + '<td class="num">' + fmt.dec(x.r.W, 1) + '</td>'
      + '<td class="num" style="color:' + (x.r.Y >= 0 ? 'var(--down)' : 'var(--up)') + '">' + fmt.wonSign(x.r.Y) + '</td>'
      + '<td class="num" style="color:' + (x.r.Z >= 0 ? 'var(--down)' : 'var(--up)') + '">' + fmt.wonSign(x.r.Z) + '</td>'
      + '<td class="num"><b style="color:' + (posAA ? 'var(--down)' : 'var(--up)') + '">' + fmt.wonSign(x.r.AA) + '</b></td></tr>';
  }).join('')
    + '<tr class="row-total"><td>합계</td><td class="num">-</td><td class="num">-</td><td class="num">-</td>'
    + '<td class="num">' + fmt.dec(c.totW, 1) + '</td>'
    + '<td class="num">' + fmt.wonSign(c.totY) + '</td>'
    + '<td class="num">' + fmt.wonSign(c.totZ) + '</td>'
    + '<td class="num">' + fmt.wonSign(c.totAA) + '</td></tr>';
}

AFTER.sim = function () {
  if (!DB.hasData()) return;
  ['ton', 'defect', 'unit', 'price'].forEach(function (id) {
    var s = $('#s_' + id);
    if (s) s.addEventListener('input', simRender);
  });
  simRender();
};

/* ====================== 이동 계획 (slide 6) ====================== */
PAGES.movement = function () {
  if (!DB.hasData()) return emptyState('이동 계획 데이터가 없습니다. 입고·출고·이송 내역 연동 후 표시됩니다.');
  var types = ['전체', '입고', '출고', '이송', '조정', '출하', '반품'];
  var typeOpts = types.map(function (t) { return '<option>' + t + '</option>'; }).join('');
  var statusOpts = ['전체', '완료', '진행중', '예정'].map(function (s) { return '<option>' + s + '</option>'; }).join('');
  var matOpts = ['전체'].concat(DB.materials.map(function (m) { return m.code + ' ' + m.name; })).map(function (s) { return '<option>' + s + '</option>'; }).join('');

  var sums = { 입고: 0, 출고: 0, 이송: 0, 조정: 0, 출하: 0, 반품: 0 };
  DB.movements.forEach(function (m) { sums[m.type] = (sums[m.type] || 0) + 1; });
  var sumCards = '<div class="grid g-3" style="margin-bottom:16px">'
    + kpiCard('입고', sums['입고'] || 0, '건', 'fa-arrow-down-to-bracket', 'ico-green', 'delta-flat', 'fa-warehouse', '원료 입고')
    + kpiCard('출고/이송', (sums['출고'] || 0) + (sums['이송'] || 0), '건', 'fa-arrow-right-arrow-left', 'ico-blue', 'delta-flat', 'fa-route', '생산/창고')
    + kpiCard('출하/반품/조정', (sums['출하'] || 0) + (sums['반품'] || 0) + (sums['조정'] || 0), '건', 'fa-truck-ramp-box', 'ico-amber', 'delta-flat', 'fa-boxes-stacked', '기타 이동')
    + '</div>';

  var filterbar = '<div class="filterbar">'
    + '<div class="field"><label>이동 유형</label><select id="f_type">' + typeOpts + '</select></div>'
    + '<div class="field"><label>자재</label><select id="f_mat">' + matOpts + '</select></div>'
    + '<div class="field"><label>상태</label><select id="f_status">' + statusOpts + '</select></div>'
    + '<div class="field"><label>검색</label><input id="f_q" type="text" placeholder="문서번호/위치 검색"></div>'
    + '<div class="grow" style="flex:1"></div>'
    + '<button class="btn"><i class="fas fa-rotate"></i> 초기화</button>'
    + '<button class="btn btn-primary"><i class="fas fa-plus"></i> 이동 등록</button>'
    + '</div>';

  var grid = '<div class="tbl-wrap"><table class="tbl"><thead><tr>'
    + '<th>문서번호</th><th>유형</th><th>자재</th><th>From</th><th>To</th>'
    + '<th class="num">계획(kg)</th><th class="num">실적(kg)</th><th class="num">단가</th><th>이동일</th><th>상태</th>'
    + '</tr></thead><tbody id="mvTbody"></tbody></table></div>';

  return sumCards + filterbar + grid;
};

function mvRender() {
  var ft = $('#f_type').value, fs = $('#f_status').value, fm = $('#f_mat').value, fq = ($('#f_q').value || '').toLowerCase();
  var list = DB.movements.filter(function (m) {
    if (ft !== '전체' && m.type !== ft) return false;
    if (fs !== '전체' && m.status !== fs) return false;
    if (fm !== '전체' && (m.mat + ' ' + m.matNm) !== fm) return false;
    if (fq && (m.id + m.from + m.to + m.matNm).toLowerCase().indexOf(fq) < 0) return false;
    return true;
  });
  var body = list.map(function (m) {
    return '<tr><td class="cell-code">' + m.id + '</td>'
      + '<td>' + tag(m.type, m.typeKey) + '</td>'
      + '<td><b>' + m.matNm + '</b> <span class="cell-code">' + m.mat + '</span></td>'
      + '<td>' + m.from + '</td><td>' + m.to + '</td>'
      + '<td class="num">' + (m.plan ? fmt.ton(m.plan) : '-') + '</td>'
      + '<td class="num">' + fmt.ton(m.actual) + '</td>'
      + '<td class="num">' + fmt.won(m.price) + '</td>'
      + '<td>' + m.date + '</td>'
      + '<td>' + tag(m.status, m.statusKey) + '</td></tr>';
  }).join('');
  $('#mvTbody').innerHTML = body || '<tr><td colspan="10" style="text-align:center;color:var(--muted);padding:30px">조건에 맞는 이동 내역이 없습니다.</td></tr>';
}

AFTER.movement = function () {
  if (!DB.hasData()) return;
  ['f_type', 'f_status', 'f_mat'].forEach(function (id) { var e = $('#' + id); if (e) e.addEventListener('change', mvRender); });
  var q = $('#f_q'); if (q) q.addEventListener('input', mvRender);
  mvRender();
};

/* ====================== SAP BW 수신 (slide 3) ====================== */
PAGES.sapsync = function () {
  if (!DB.hasData()) return emptyState('SAP BW 수신 내역이 없습니다. SAP 연동 후 표준원단위·표준단가·생산계획이 표시됩니다.');
  var kpis = '<div class="grid g-3" style="margin-bottom:16px">'
    + kpiCard('수신 자재', DB.materials.length, '개', 'fa-cubes', 'ico-blue', 'delta-up', 'fa-check', '정상 수신')
    + kpiCard('최근 수신', '06:14', '', 'fa-clock', 'ico-indigo', 'delta-flat', 'fa-calendar', '2026-05-01')
    + kpiCard('연동 상태', '정상', '', 'fa-plug-circle-check', 'ico-green', 'delta-up', 'fa-wifi', 'SAP RFC')
    + '</div>';
  var card = '<div class="card"><div class="card__head"><h3>SAP BW 수신 내역</h3>'
    + '<span class="sub">표준원단위(L)·표준단가(M)·생산계획</span><div class="grow"></div>'
    + tag('SAP 자동', 'INBOUND') + '<button class="btn btn-sm" style="margin-left:10px"><i class="fas fa-rotate"></i> 재수신</button></div>'
    + '<div class="tbl-wrap" style="border:none;border-radius:0"><table class="tbl"><thead><tr>'
    + '<th>자재코드</th><th>자재명</th><th>항목</th><th class="num">값</th><th>출처</th><th>수신시각</th>'
    + '</tr></thead><tbody>'
    + DB.bwRows.map(function (r) {
      return '<tr><td class="cell-code">' + r.code + '</td><td><b>' + r.name + '</b></td><td>' + r.field + '</td>'
        + '<td class="num">' + (typeof r.value === 'number' ? fmt.dec(r.value, r.value < 100 ? 4 : 2) : r.value) + '</td>'
        + '<td>' + tag(r.src, 'INBOUND') + '</td><td class="cell-code">' + r.recv + '</td></tr>';
    }).join('')
    + '</tbody></table></div></div>';
  return kpis + card;
};

/* ====================== 자재 마스터 (slide) ====================== */
PAGES.master = function () {
  if (!DB.hasData()) return emptyState('자재 마스터 데이터가 없습니다. 기준정보 연동 후 가중평균 단가가 표시됩니다.');
  var rows = DB.materials.map(function (m) {
    var V = Calc.weightedAvgPrice(m.R, m.S, m.T, m.U);
    return '<tr><td class="cell-code">' + m.code + '</td><td><b>' + m.name + '</b></td><td>' + m.maker + '</td>'
      + '<td>' + tag(m.group, 'TRANSFER') + '</td>'
      + '<td class="num">' + fmt.dec(m.L, 4) + '</td>'
      + '<td class="num">' + fmt.dec(m.M, 2) + '</td>'
      + '<td class="num">' + fmt.ton(m.R) + ' @ ' + fmt.won(m.S) + '</td>'
      + '<td class="num">' + fmt.ton(m.T) + ' @ ' + fmt.won(m.U) + '</td>'
      + '<td class="num"><b style="color:var(--blue-700)">' + fmt.dec(V, 2) + '</b></td></tr>';
  }).join('');
  return '<div class="note" style="margin-bottom:16px"><i class="fas fa-circle-info"></i> 가중평균단가 V = (재고량R×재고단가S + 입고량T×입고단가U) / (R+T)</div>'
    + '<div class="tbl-wrap"><table class="tbl"><thead><tr>'
    + '<th>자재코드</th><th>자재명</th><th>공급사</th><th>그룹</th>'
    + '<th class="num">표준원단위 L</th><th class="num">표준단가 M</th>'
    + '<th class="num">재고 (R@S)</th><th class="num">입고 (T@U)</th><th class="num">가중단가 V</th>'
    + '</tr></thead><tbody>' + rows + '</tbody></table></div>';
};

/* ====================== 생산·원가 계획 (slide 5) ====================== */
PAGES.plan = function () {
  if (!DB.hasData()) return emptyState('생산·원가 계획 데이터가 없습니다. 계획 등록 또는 API 연동 후 표시됩니다.');
  var rows = DB.plans.map(function (p) {
    return '<tr><td class="cell-code">' + p.id + '</td><td><b>' + p.plant + '</b></td>'
      + '<td>' + tag(p.grade, 'TRANSFER') + '</td>'
      + '<td class="num">' + p.bw + ' g/m²</td>'
      + '<td class="num">' + fmt.ton(p.ton) + ' 톤</td>'
      + '<td>' + p.owner + '</td><td>' + p.updated + '</td>'
      + '<td>' + tag(p.status, p.statusKey) + '</td>'
      + '<td><button class="btn btn-sm" onclick="location.hash=\'sim\'"><i class="fas fa-sliders"></i> 시뮬레이션</button></td></tr>';
  }).join('');
  return '<div class="filterbar"><div class="grow" style="flex:1"></div>'
    + '<button class="btn btn-primary"><i class="fas fa-plus"></i> 계획 등록</button></div>'
    + '<div class="tbl-wrap"><table class="tbl"><thead><tr>'
    + '<th>계획번호</th><th>호기</th><th>지종</th><th class="num">평량</th><th class="num">생산량</th>'
    + '<th>담당</th><th>수정일</th><th>상태</th><th></th></tr></thead><tbody>' + rows + '</tbody></table></div>';
};

/* ====================== 실적 입력 (slide 7) ====================== */
PAGES.actual = function () {
  if (!DB.hasData()) return emptyState('실적 입력 대상 자재가 없습니다. 자재 마스터 연동 후 실적원단위를 입력할 수 있습니다.');
  var rows = DB.materials.map(function (m) {
    var diff = ((m.Q - m.L) / m.L * 100);
    return '<tr><td class="cell-code">' + m.code + '</td><td><b>' + m.name + '</b></td>'
      + '<td class="num">' + fmt.dec(m.L, 4) + '</td>'
      + '<td class="num"><input type="text" value="' + fmt.dec(m.Q, 4) + '" style="width:90px;text-align:right;border:1px solid var(--border-2);border-radius:6px;padding:4px 8px;font-family:inherit"></td>'
      + '<td class="num" style="color:' + (diff >= 0 ? 'var(--down)' : 'var(--up)') + '"><b>' + fmt.pctSign(diff) + '</b></td>'
      + '<td>' + (Math.abs(diff) > 2 ? tag('초과', 'REJECTED') : tag('정상', 'DONE')) + '</td></tr>';
  }).join('');
  return '<div class="note" style="margin-bottom:16px"><i class="fas fa-pen-to-square"></i> 실적원단위(Q)를 입력하세요. 표준 대비 ±2% 초과 시 경고 표시됩니다. ' + tag('USR 입력', 'IN_PROGRESS') + '</div>'
    + '<div class="tbl-wrap" style="margin-bottom:16px"><table class="tbl"><thead><tr>'
    + '<th>자재코드</th><th>자재명</th><th class="num">표준 L</th><th class="num">실적 Q (입력)</th><th class="num">차이율</th><th>판정</th>'
    + '</tr></thead><tbody>' + rows + '</tbody></table></div>'
    + '<button class="btn btn-primary"><i class="fas fa-floppy-disk"></i> 실적 저장</button> '
    + '<button class="btn" onclick="location.hash=\'sim\'"><i class="fas fa-arrow-right"></i> 시뮬레이션으로</button>';
};

/* ====================== 승인 워크플로 (slide 10) ====================== */
PAGES.approval = function () {
  if (!DB.hasData()) return emptyState('승인 워크플로 데이터가 없습니다. 사전원가 상신 후 결재 현황이 표시됩니다.');
  var rows = DB.approvals.map(function (a) {
    return '<tr><td class="cell-code">' + a.id + '</td><td><b>' + a.title + '</b></td>'
      + '<td class="num">' + fmt.dec(a.amount / 1000, 2) + ' 억원</td>'
      + '<td>' + a.requester + '</td><td>' + a.step + '</td><td>' + a.date + '</td>'
      + '<td>' + tag(a.status, a.statusKey) + '</td>'
      + '<td>' + (a.statusKey === 'SUBMITTED'
        ? '<button class="btn btn-sm btn-primary"><i class="fas fa-check"></i> 승인</button> <button class="btn btn-sm"><i class="fas fa-xmark"></i></button>'
        : '<button class="btn btn-sm"><i class="fas fa-eye"></i> 보기</button>') + '</td></tr>';
  }).join('');
  var kpis = '<div class="grid g-4" style="margin-bottom:16px">'
    + kpiCard('상신 대기', DB.approvals.filter(function (a) { return a.statusKey === 'SUBMITTED'; }).length, '건', 'fa-hourglass-half', 'ico-amber', 'delta-flat', 'fa-clock', '내 승인')
    + kpiCard('승인 완료', DB.approvals.filter(function (a) { return a.statusKey === 'APPROVED'; }).length, '건', 'fa-circle-check', 'ico-green', 'delta-up', 'fa-check', '이번 달')
    + kpiCard('반려', DB.approvals.filter(function (a) { return a.statusKey === 'REJECTED'; }).length, '건', 'fa-circle-xmark', 'ico-red', 'delta-flat', 'fa-rotate-left', '재작성 필요')
    + kpiCard('초안', DB.approvals.filter(function (a) { return a.statusKey === 'DRAFT'; }).length, '건', 'fa-file-pen', 'ico-blue', 'delta-flat', 'fa-pen', '작성 중')
    + '</div>';
  return kpis + '<div class="tbl-wrap"><table class="tbl"><thead><tr>'
    + '<th>문서번호</th><th>제목</th><th class="num">금액</th><th>요청자</th><th>단계</th><th>일자</th><th>상태</th><th>처리</th>'
    + '</tr></thead><tbody>' + rows + '</tbody></table></div>';
};

/* ====================== 전력비 시뮬레이션 (엑셀 05.이동계획) ====================== */
/* 데이터 소스: power-data.js의 POWER_DEMO. DEMO_MODE면 데모데이터, 운영이면 빈 상태.
   PowerDB는 운영에서 PowerDB.load(payload)로 채움. */
var PowerDB = (typeof DEMO_MODE !== 'undefined' && DEMO_MODE && typeof POWER_DEMO !== 'undefined')
  ? POWER_DEMO
  : { lines: [], months: [], lineType: {}, byMonth: {}, unit: {}, group: {} };
PowerDB.hasData = function () { return this.months && this.months.length > 0; };
PowerDB.load = function (payload) {
  if (!payload) return this;
  var self = this;
  ['lines', 'months', 'lineType', 'byMonth', 'unit', 'group'].forEach(function (k) { if (payload[k] != null) self[k] = payload[k]; });
  return this;
};

/* ====================== 예상 생산량 기준정보 (전역 공유) ======================
   ProdPlan.plan[month][line] = { prod?, hours? }  (예상 생산량 / 예상 가동시간)
   - 항목별로 입력값이 있으면 그 값, 없으면 PowerDB의 실적값을 fallback.
   - 생산성(생산량/일) = 생산량 ÷ (가동시간 ÷ 24)  → 자동 계산(저장 안 함)
   - 전력비(A방식): 시간당전력 × 예상가동시간 → 예상사용량, ÷예상생산량 → 원단위
   - 전력비·(추후)재료비 등 모든 시뮬레이션이 이 값을 공유한다. */
var ProdPlan = {
  STORAGE_KEY: 'precost_prodplan_v2',
  plan: {},
  loadStorage: function () {
    try {
      var raw = localStorage.getItem(this.STORAGE_KEY);
      if (raw) this.plan = JSON.parse(raw) || {};
    } catch (e) { this.plan = {}; }
  },
  saveStorage: function () {
    try { localStorage.setItem(this.STORAGE_KEY, JSON.stringify(this.plan)); } catch (e) {}
  },
  // 실적값 (PowerDB 기준). field: 'prod' | 'hours'
  actual: function (month, line, field) {
    var rec = (PowerDB.byMonth || {})[month];
    if (rec && rec.lines && rec.lines[line]) {
      var v = rec.lines[line][field];
      return (v == null) ? null : v;
    }
    return null;
  },
  // 입력 레코드 ({prod?,hours?} 또는 undefined)
  rec: function (month, line) {
    return (this.plan[month] && this.plan[month][line]) ? this.plan[month][line] : null;
  },
  // 항목이 명시적으로 입력되었는지
  isSet: function (month, line, field) {
    var r = this.rec(month, line);
    return !!(r && r[field] != null);
  },
  // 해당 호기에 (생산량 또는 가동시간 중) 하나라도 입력값이 있는지
  isAnySet: function (month, line) {
    return this.isSet(month, line, 'prod') || this.isSet(month, line, 'hours');
  },
  // 예상값 (입력값 있으면 그 값, 없으면 실적값). field: 'prod' | 'hours'
  get: function (month, line, field) {
    var r = this.rec(month, line);
    if (r && r[field] != null) return r[field];
    return this.actual(month, line, field);
  },
  // 생산성 (생산량/일) = 생산량 ÷ (가동시간/24)
  productivity: function (month, line) {
    var prod = this.get(month, line, 'prod');
    var hours = this.get(month, line, 'hours');
    if (prod == null || hours == null || hours <= 0) return null;
    return prod / (hours / 24);
  },
  set: function (month, line, field, val) {
    if (!this.plan[month]) this.plan[month] = {};
    if (!this.plan[month][line]) this.plan[month][line] = {};
    if (val == null || val === '') { delete this.plan[month][line][field]; }
    else { this.plan[month][line][field] = +val; }
    // 빈 레코드 정리
    var r = this.plan[month][line];
    if (r && r.prod == null && r.hours == null) { delete this.plan[month][line]; }
    if (this.plan[month] && Object.keys(this.plan[month]).length === 0) { delete this.plan[month]; }
    this.saveStorage();
  },
  clearMonth: function (month) {
    delete this.plan[month];
    this.saveStorage();
  },
  // 엑셀 업로드 결과 일괄 반영: rows = [{month, line, prod?, hours?}]
  bulkSet: function (rows) {
    var self = this, applied = 0, skipped = 0;
    rows.forEach(function (r) {
      if (!r.month || !r.line) { skipped++; return; }
      if (PowerDB.lines.indexOf(r.line) < 0) { skipped++; return; }
      if (PowerDB.months.indexOf(r.month) < 0) { skipped++; return; }
      if (typeof Period !== 'undefined' && Period.isActual(r.month)) { skipped++; return; } // 실적 확정월 — 업로드 무시
      if (!self.plan[r.month]) self.plan[r.month] = {};
      if (!self.plan[r.month][r.line]) self.plan[r.month][r.line] = {};
      var any = false;
      if (r.prod != null && r.prod !== '') { self.plan[r.month][r.line].prod = +r.prod; any = true; }
      if (r.hours != null && r.hours !== '') { self.plan[r.month][r.line].hours = +r.hours; any = true; }
      if (any) applied++; else skipped++;
    });
    this.saveStorage();
    return { applied: applied, skipped: skipped };
  }
};
ProdPlan.loadStorage();

var ProdPlanState = { month: null };

var PowerState = { month: null, price: null };

// 기본 표시 월: 기준월(현재월)이 데이터에 있으면 그 달, 없으면 첫 달
function defaultMonth() {
  if (PowerDB.months && PowerDB.months.indexOf(Period.cutoff) >= 0) return Period.cutoff;
  return (PowerDB.months || [])[0];
}

function powerCurrentMonthRec() {
  var m = PowerState.month || defaultMonth();
  return { key: m, rec: (PowerDB.byMonth || {})[m] };
}

PAGES.power = function () {
  if (!PowerDB.hasData()) return emptyState('전력비 실적 데이터가 없습니다. 운영에서는 SAP/엑셀 연동 후 호기별 전력 실적이 표시됩니다.');

  var monthOpts = PowerDB.months.map(function (m) {
    var sel = (m === (PowerState.month || defaultMonth())) ? ' selected' : '';
    var label = m.replace('-', '년 ') + '월' + (Period.isActual(m) ? ' · 실적' : ' · 예상');
    return '<option value="' + m + '"' + sel + '>' + label + '</option>';
  }).join('');

  var cur = powerCurrentMonthRec();
  var curIsActual = Period.isActual(cur.key);
  var price = (PowerState.price != null) ? PowerState.price : (cur.rec ? cur.rec.priceAcct : 0);

  var stepper = '<div class="stepper">'
    + '<div class="step done"><div class="step__dot"><i class="fas fa-check"></i></div><div class="step__lab">실적 수신</div></div><div class="step__line"></div>'
    + '<div class="step done"><div class="step__dot"><i class="fas fa-check"></i></div><div class="step__lab">단가 확정</div></div><div class="step__line"></div>'
    + '<div class="step current"><div class="step__dot">3</div><div class="step__lab">생산량 시뮬레이션</div></div><div class="step__line"></div>'
    + '<div class="step"><div class="step__dot">4</div><div class="step__lab">전력비 산출</div></div></div>';

  var planSetCnt = PowerDB.lines.filter(function (ln) { return ProdPlan.isAnySet(cur.key, ln); }).length;
  var controls = '<div class="card"><div class="card__head"><h3><i class="fas fa-bolt" style="color:var(--blue);margin-right:7px"></i>시뮬레이션 입력</h3>'
    + '<div class="grow"></div>' + tag('실적 기반', 'INBOUND') + '</div><div class="card__body">'
    + '<div class="field" style="margin-bottom:12px"><label style="display:block;font-size:12px;color:var(--muted);margin-bottom:5px">대상 월 <span style="color:var(--muted-2);font-size:11px">(기준월: ' + Period.cutoff.replace('-', '.') + ' · 전달까지 실적)</span></label>'
    + '<select id="pw_month" style="width:100%;padding:9px 11px;border:1px solid var(--border-2);border-radius:8px;font-family:inherit">' + monthOpts + '</select></div>'
    + '<div id="pw_period" class="note" style="margin-bottom:12px;border-color:' + (curIsActual ? 'var(--muted-2,#94a3b8)' : 'var(--blue,#2563eb)') + ';color:' + (curIsActual ? 'var(--muted,#64748b)' : 'var(--blue-700,#1d4ed8)') + '"></div>'
    + '<div class="field" style="margin-bottom:6px"><label style="display:block;font-size:12px;color:var(--muted);margin-bottom:5px">전력단가 (회계비용 기준) [원/kWh]</label>'
    + '<input id="pw_price" type="number" step="0.01" value="' + price.toFixed(2) + '"' + (curIsActual ? ' disabled title="실적 확정월은 단가 조정 불가"' : '') + ' style="width:100%;padding:9px 11px;border:1px solid var(--border-2);border-radius:8px;font-family:inherit;text-align:right' + (curIsActual ? ';background:#f1f5f9;color:#94a3b8' : '') + '"></div>'
    + '<div class="note" style="margin:12px 0"><i class="fas fa-circle-info"></i> 생산량·가동시간은 <b>예상 생산량 기준정보</b> 탭에서 입력합니다. 이 화면은 그 값을 자동 반영합니다. '
    + '<br>전력사용량은 <b>가동시간 비례(A방식)</b>로 예측됩니다. '
    + (planSetCnt > 0 ? '<b style="color:var(--blue-700)">(예상값 ' + planSetCnt + '개 호기 적용 중)</b>' : '<span style="color:var(--muted-2)">(현재 실적값 사용)</span>') + '</div>'
    + '<button class="btn" style="width:100%" onclick="location.hash=\'prodplan\'"><i class="fas fa-industry"></i> 예상 생산량 기준정보 편집</button>'
    + '<hr class="hr">'
    + '<button class="btn" style="width:100%" id="pw_resetprice"><i class="fas fa-rotate-left"></i> 단가 실적값으로 초기화</button>'
    + '<button class="btn btn-primary" style="width:100%;margin-top:9px" onclick="location.hash=\'approval\'"><i class="fas fa-paper-plane"></i> 전력비 결과 상신</button>'
    + '</div></div>';

  var kpis = '<div class="grid g-3" id="pwKpis"></div>';
  var tbl = '<div class="card"><div class="card__head"><h3>호기별 전력비 상세</h3><div class="grow"></div>'
    + '<span class="legend"><span class="l">' + tag('제지/화장지', 'TRANSFER') + 'kWh/ton</span><span class="l">' + tag('가공/생리대', 'SHIPMENT') + 'kWh/개</span></span></div>'
    + '<div class="tbl-wrap" style="border:none;border-radius:0"><table class="tbl"><thead><tr>'
    + '<th>호기</th><th class="num">가동시간 [Hr]</th><th class="num">전력사용량 [kWh]</th><th class="num">생산량</th><th class="num">전력원단위</th>'
    + '<th class="num">전력비원단위</th><th class="num">전력비 [원]</th><th>변동</th>'
    + '</tr></thead><tbody id="pwTbody"></tbody></table></div></div>';

  return stepper
    + '<div class="grid" style="grid-template-columns:360px 1fr;gap:16px;align-items:start">'
    + controls
    + '<div class="grid" style="gap:16px">' + kpis + tbl + '</div>'
    + '</div>';
};

function powerRender() {
  var cur = powerCurrentMonthRec();
  if (!cur.rec) return;
  // 실적 확정월(과거)은 SAP 실적 그대로 — ProdPlan 예상값을 적용하지 않는다(수정 금지).
  // 예상월(현재월 이후)만 ProdPlan(예상 생산량 기준정보)을 자동 반영(A방식).
  var isActual = Period.isActual(cur.key);
  var ov = {};
  if (!isActual) {
    PowerDB.lines.forEach(function (ln) {
      var o = {};
      var p = ProdPlan.get(cur.key, ln, 'prod'); if (p != null) o.prod = p;
      var h = ProdPlan.get(cur.key, ln, 'hours'); if (h != null) o.hours = h;
      ov[ln] = o;
    });
    if (PowerState.price != null) ov.price = PowerState.price;
  }
  // 기간 배너 갱신
  var pb = $('#pw_period');
  if (pb) {
    pb.innerHTML = isActual
      ? '<i class="fas fa-lock"></i> <b>' + cur.key.replace('-', '.') + '</b> 은(는) <b>실적 확정월</b>입니다. SAP 실적이 그대로 표시되며 예상값/단가 조정은 적용되지 않습니다.'
      : '<i class="fas fa-wand-magic-sparkles"></i> <b>' + cur.key.replace('-', '.') + '</b> 은(는) <b>예상월</b>입니다. 예상 생산량 기준정보(이동계획) + 가동시간 비례(A방식)로 예측됩니다.';
    pb.style.borderColor = isActual ? 'var(--muted-2,#94a3b8)' : 'var(--blue,#2563eb)';
    pb.style.color = isActual ? 'var(--muted,#64748b)' : 'var(--blue-700,#1d4ed8)';
  }
  var res = PowerCalc.computeMonth(cur.rec, PowerDB.lines, PowerDB.lineType, ov);

  // KPIs
  var totalCost = res.totals.cost;
  var totalUsage = res.totals.usage;
  var perTon = res.totals.paperCostPerTon;
  $('#pwKpis').innerHTML =
    kpiCard('총 전력비', pfmt.baek(totalCost), '백만원', 'fa-won-sign', 'ico-blue', 'delta-flat', 'fa-bolt', cur.key.replace('-', '.') + ' 기준')
    + kpiCard('총 전력사용량', pfmt.baek(totalUsage * 1000) , '백만kWh', 'fa-plug', 'ico-indigo', 'delta-flat', 'fa-gauge', '회계 단가 ' + res.price.toFixed(2) + '원')
    + kpiCard('제지·화장지 톤당 전력비', pfmt.won(perTon), '원/톤', 'fa-weight-hanging', 'ico-amber', 'delta-flat', 'fa-industry', 'paper 군 가중');

  // table
  $('#pwTbody').innerHTML = res.rows.map(function (r) {
    if (r.baseUsage === 0 && r.baseProd === 0) return '';
    var isPaper = (r.type === 'paper');
    var unitU = isPaper ? 'kWh/ton' : 'kWh/개';
    var costU = isPaper ? '천원/ton' : '원/개';
    var prodU = isPaper ? 'kg' : 'EA';
    // 변동: 생산량 또는 가동시간이 실적과 다른지
    var prodChanged = Math.abs(r.prod - r.baseProd) > 0.5;
    var hoursChanged = (r.planHours != null && r.baseHours != null) ? (Math.abs(r.planHours - r.baseHours) > 0.05) : false;
    var deltaPct = r.baseProd > 0 ? ((r.prod - r.baseProd) / r.baseProd * 100) : 0;
    var deltaCell;
    if (prodChanged || hoursChanged) {
      deltaCell = '<span class="tag ' + (deltaPct >= 0 ? 'tag-up' : 'tag-down') + '">' + (deltaPct >= 0 ? '+' : '') + deltaPct.toFixed(1) + '%</span>';
    } else {
      deltaCell = '<span style="color:var(--muted-2);font-size:11px">실적</span>';
    }
    // 가동시간 셀 (B방식 fallback이면 표시)
    var hoursCell = (r.planHours != null)
      ? pfmt.dec(r.planHours, 0)
      : '<span style="color:var(--muted-2);font-size:11px" title="실적 가동시간 없음 → 생산량 기준(B방식)">N/A</span>';
    var modeBadge = (r.usageMode === 'B') ? ' <span class="cell-code" title="가동시간 실적 없음: 실적 전력사용량 그대로 사용">B</span>' : '';
    return '<tr><td><b>' + r.line + '</b> <span class="cell-code">' + (isPaper ? 'PAPER' : 'PROC') + '</span></td>'
      + '<td class="num">' + hoursCell + '</td>'
      + '<td class="num">' + pfmt.int(r.usage) + modeBadge + '</td>'
      + '<td class="num">' + pfmt.int(r.prod) + ' <span style="color:var(--muted-2);font-size:10px">' + prodU + '</span></td>'
      + '<td class="num">' + pfmt.dec(r.unit, isPaper ? 2 : 4) + ' <span style="color:var(--muted-2);font-size:10px">' + unitU + '</span></td>'
      + '<td class="num">' + pfmt.dec(r.costUnit, isPaper ? 2 : 4) + ' <span style="color:var(--muted-2);font-size:10px">' + costU + '</span></td>'
      + '<td class="num"><b>' + pfmt.won(r.cost) + '</b></td>'
      + '<td>' + deltaCell + '</td></tr>';
  }).join('')
    + '<tr class="row-total"><td>합계</td><td class="num">-</td><td class="num">' + pfmt.int(res.totals.usage) + '</td>'
    + '<td class="num">-</td><td class="num">-</td><td class="num">-</td>'
    + '<td class="num">' + pfmt.won(res.totals.cost) + '</td><td>-</td></tr>';
}

AFTER.power = function () {
  if (!PowerDB.hasData()) return;

  var ms = $('#pw_month');
  if (ms) ms.addEventListener('change', function () {
    PowerState.month = ms.value;
    PowerState.price = null;
    route(); // 월 변경 시 단가 리셋 + planSetCnt 갱신 위해 전체 재렌더
  });

  var ps = $('#pw_price');
  if (ps) ps.addEventListener('input', function () {
    PowerState.price = (ps.value === '') ? null : +ps.value;
    powerRender();
  });

  var rb = $('#pw_resetprice');
  if (rb) rb.addEventListener('click', function () {
    PowerState.price = null;
    route();
  });

  powerRender();
};

/* ====================== 예상 생산량 기준정보 화면 (#prodplan) ======================
   - 호기별 3행: 생산량 / 가동시간(Hr) / 생산성(생산량/일, 자동)
   - 생산량 단위는 호기별로 다름(제지·화장지=톤, 가공·생리대=천개, 라미네이팅=EA)
     · 내부(PowerDB)는 kg/EA 단위 → 화면/입력은 표시단위(÷1000) 기준, 저장 시 내부단위로 변환
   - 생산성 = 생산량 ÷ (가동시간 ÷ 24) [표시단위/일]
   - 입력 안 한 항목은 실적값 그대로 사용 */
function prodplanCurrentMonth() {
  return ProdPlanState.month || defaultMonth();
}
// 표시단위/입력단위 환산: 제지·화장지=톤(=kg/1000), 가공·생리대=천개(=EA/1000), 라미네이팅=EA(환산 1)
function ppDispUnit(ln) { return (PowerDB.unit && PowerDB.unit[ln]) || ((PowerDB.lineType[ln] === 'paper') ? '톤' : 'EA'); }
function ppFactor(ln) { var u = ppDispUnit(ln); return (u === '톤' || u === '천개') ? 1000 : 1; } // 내부 = 표시 × factor
function ppToDisp(ln, internal) { if (internal == null) return null; return internal / ppFactor(ln); }
function ppToInternal(ln, disp) { if (disp == null || disp === '') return null; return (+disp) * ppFactor(ln); }
// 그룹 순서·소계용
var PP_GROUP_ORDER = ['제지', '화장지', '가공', '생리대', '라미네이팅'];

PAGES.prodplan = function () {
  if (!PowerDB.hasData()) return emptyState('생산량 기준이 되는 실적 데이터가 없습니다. 운영에서는 SAP/엑셀 연동 후 호기별 실적 생산량·가동시간이 표시됩니다.');

  var monthOpts = PowerDB.months.map(function (m) {
    var sel = (m === prodplanCurrentMonth()) ? ' selected' : '';
    return '<option value="' + m + '"' + sel + '>' + m.replace('-', '년 ') + '월' + (Period.isActual(m) ? ' · 실적' : ' · 예상') + '</option>';
  }).join('');

  var cur = prodplanCurrentMonth();
  var curIsActual = Period.isActual(cur);
  var setCnt = PowerDB.lines.filter(function (ln) { return ProdPlan.isAnySet(cur, ln); }).length;

  var head = '<div class="card"><div class="card__head"><h3><i class="fas fa-industry" style="color:var(--blue);margin-right:7px"></i>예상 생산량 기준정보 (이동계획)</h3>'
    + '<div class="grow"></div>' + tag('전 시뮬레이션 공통', 'INBOUND') + '</div><div class="card__body">'
    + '<div class="note" style="margin-bottom:14px"><i class="fas fa-circle-info"></i> 호기별 <b>예상 생산량</b>과 <b>가동시간</b>을 입력하면 전력비 등 모든 원가 시뮬레이션의 공통 기준이 됩니다. '
    + '<b>생산성(생산량/일)</b>은 자동 계산되며, 비워둔 항목은 <b>실적값</b>을 그대로 사용합니다.<br>'
    + '전력사용량은 <b>가동시간 비례(A방식)</b>로 예측됩니다: 시간당 전력(실적) × 예상 가동시간.</div>'
    + '<div class="note" style="margin-bottom:14px;border-color:' + (curIsActual ? 'var(--muted-2,#94a3b8)' : 'var(--blue,#2563eb)') + ';color:' + (curIsActual ? 'var(--muted,#64748b)' : 'var(--blue-700,#1d4ed8)') + '">'
    + (curIsActual
        ? '<i class="fas fa-lock"></i> <b>' + cur.replace('-', '.') + '</b> 은(는) <b>실적 확정월</b>입니다(기준월 ' + Period.cutoff.replace('-', '.') + ' 전달까지). SAP 실적이며 <b>입력/수정이 금지</b>됩니다.'
        : '<i class="fas fa-pen-to-square"></i> <b>' + cur.replace('-', '.') + '</b> 은(는) <b>예상월</b>입니다(기준월 ' + Period.cutoff.replace('-', '.') + ' 포함 이후). 이동계획에 따라 자유롭게 입력하세요.') + '</div>'
    + '<div class="grid" style="grid-template-columns:1fr 1fr;gap:12px">'
    + '<div class="field"><label style="display:block;font-size:12px;color:var(--muted);margin-bottom:5px">대상 월</label>'
    + '<select id="pp_month" style="width:100%;padding:9px 11px;border:1px solid var(--border-2);border-radius:8px;font-family:inherit">' + monthOpts + '</select></div>'
    + '<div class="field" style="display:flex;align-items:flex-end;gap:8px">'
    + '<button class="btn" style="flex:1" id="pp_template"><i class="fas fa-download"></i> 엑셀 양식 받기</button>'
    + '<label class="btn btn-primary" style="flex:1;text-align:center;cursor:pointer;margin:0' + (curIsActual ? ';opacity:.45;pointer-events:none' : '') + '"><i class="fas fa-file-import"></i> 엑셀 업로드'
    + '<input type="file" id="pp_file" accept=".xlsx,.xls,.csv" style="display:none"' + (curIsActual ? ' disabled' : '') + '></label></div>'
    + '</div>'
    + '<div id="pp_uploadmsg" style="margin-top:10px"></div>'
    + '</div></div>';

  var summary = '<div class="grid g-3" id="ppKpis" style="margin-top:16px"></div>';

  var tbl = '<div class="card" style="margin-top:16px"><div class="card__head"><h3>호기별 예상 생산량·가동시간 입력 <span style="font-size:12px;color:var(--muted)">(' + cur.replace('-', '.') + ')</span></h3>'
    + '<div class="grow"></div>'
    + '<span id="pp_setbadge">' + (curIsActual ? tag('실적 확정', 'INBOUND') : (setCnt > 0 ? tag('예상값 ' + setCnt + '개 호기', 'TRANSFER') : tag('전부 실적값', 'SHIPMENT'))) + '</span>'
    + (curIsActual ? '' : '<button class="btn" id="pp_resetmonth" style="margin-left:9px"><i class="fas fa-rotate-left"></i> 이 달 전체 실적값으로</button>')
    + '</div>'
    + '<div class="tbl-wrap" style="border:none;border-radius:0"><table class="tbl"><thead><tr>'
    + '<th>호기</th><th>항목</th><th class="num">단위</th><th class="num">실적</th><th class="num">예상 (입력)</th><th class="num">증감</th>'
    + '</tr></thead><tbody id="ppTbody"></tbody></table></div>'
    + '<div class="note" style="margin:10px 14px 4px"><i class="fas fa-calculator"></i> 생산성(생산량/일) = 생산량 ÷ (가동시간 ÷ 24) · 자동 계산</div></div>';

  return head + summary + tbl;
};

// 가동 여부(실적 생산량 또는 사용량이 있는 호기만 표시)
function ppActiveLine(month, ln) {
  var rec = (PowerDB.byMonth || {})[month];
  if (!rec || !rec.lines || !rec.lines[ln]) return false;
  var l = rec.lines[ln];
  return (l.prod > 0) || (l.usage > 0) || (l.hours != null && l.hours > 0);
}

function prodplanRenderTable() {
  var cur = prodplanCurrentMonth();
  var locked = Period.isActual(cur); // 실적 확정월 → 입력 비활성
  var lockAttr = locked ? ' disabled title="실적 확정월 — 수정 금지"' : '';
  var lockStyle = locked ? ';background:#f1f5f9;color:#94a3b8;cursor:not-allowed' : '';
  var html = '';
  var lastGroup = null;

  PowerDB.lines.forEach(function (ln) {
    if (!ppActiveLine(cur, ln)) return;
    var group = (PowerDB.group && PowerDB.group[ln]) || '';
    var unit = ppDispUnit(ln);

    // 그룹 헤더(첫 호기 진입 시)
    if (group !== lastGroup) {
      html += '<tr class="row-group"><td colspan="6" style="background:var(--bg-2,#f1f5f9);font-weight:700;color:var(--blue-700,#1d4ed8);font-size:12px;padding:7px 12px">' + group + '</td></tr>';
      lastGroup = group;
    }

    // 실적/예상 값 (표시단위)
    var actProdDisp = ppToDisp(ln, ProdPlan.actual(cur, ln, 'prod'));
    var actHours = ProdPlan.actual(cur, ln, 'hours');
    var planProdSet = ProdPlan.isSet(cur, ln, 'prod');
    var planHoursSet = ProdPlan.isSet(cur, ln, 'hours');
    var planProdDisp = planProdSet ? ppToDisp(ln, ProdPlan.rec(cur, ln).prod) : '';
    var planHoursVal = planHoursSet ? ProdPlan.rec(cur, ln).hours : '';

    // 증감(생산량)
    var prodDelta = (planProdSet && actProdDisp) ? ((planProdDisp - actProdDisp) / actProdDisp * 100) : null;
    var prodDeltaCell = (prodDelta != null)
      ? '<span class="tag ' + (prodDelta >= 0 ? 'tag-up' : 'tag-down') + '">' + (prodDelta >= 0 ? '+' : '') + prodDelta.toFixed(1) + '%</span>'
      : '<span style="color:var(--muted-2);font-size:11px">실적</span>';
    // 증감(가동시간)
    var hoursDelta = (planHoursSet && actHours) ? ((planHoursVal - actHours) / actHours * 100) : null;
    var hoursDeltaCell = (hoursDelta != null)
      ? '<span class="tag ' + (hoursDelta >= 0 ? 'tag-up' : 'tag-down') + '">' + (hoursDelta >= 0 ? '+' : '') + hoursDelta.toFixed(1) + '%</span>'
      : (actHours != null ? '<span style="color:var(--muted-2);font-size:11px">실적</span>' : '<span style="color:var(--muted-2);font-size:11px">-</span>');

    // 생산성(자동)
    var actProductivity = (function () {
      var p = ProdPlan.actual(cur, ln, 'prod'), h = ProdPlan.actual(cur, ln, 'hours');
      if (p == null || h == null || h <= 0) return null;
      return ppToDisp(ln, p) / (h / 24);
    })();
    var planProductivity = (function () {
      var p = ProdPlan.get(cur, ln, 'prod'), h = ProdPlan.get(cur, ln, 'hours');
      if (p == null || h == null || h <= 0) return null;
      return ppToDisp(ln, p) / (h / 24);
    })();

    var rowspan = ' rowspan="3" style="vertical-align:middle"';
    // 행1: 생산량
    html += '<tr data-line="' + ln + '" class="pp-rowtop">'
      + '<td' + rowspan + '><b>' + ln + '</b> <span class="cell-code">' + (PowerDB.lineType[ln] === 'paper' ? 'PAPER' : 'PROC') + '</span></td>'
      + '<td>생산량</td>'
      + '<td class="num"><span style="color:var(--muted-2);font-size:11px">' + unit + '</span></td>'
      + '<td class="num">' + (actProdDisp != null ? pfmt.int(actProdDisp) : '-') + '</td>'
      + '<td class="num"><input type="number" class="pp_in" data-line="' + ln + '" data-field="prod" value="' + (planProdDisp === '' ? '' : planProdDisp) + '" placeholder="' + (actProdDisp != null ? pfmt.int(actProdDisp) : '') + '"' + lockAttr + ' '
      + 'style="width:120px;padding:5px 8px;border:1px solid var(--border-2);border-radius:7px;font-family:inherit;text-align:right;font-size:12.5px' + lockStyle + '"></td>'
      + '<td class="pp-delta-prod">' + prodDeltaCell + '</td></tr>';
    // 행2: 가동시간
    html += '<tr data-line="' + ln + '">'
      + '<td>가동시간</td>'
      + '<td class="num"><span style="color:var(--muted-2);font-size:11px">Hr</span></td>'
      + '<td class="num">' + (actHours != null ? pfmt.int(actHours) : '<span style="color:var(--muted-2);font-size:11px">N/A</span>') + '</td>'
      + '<td class="num"><input type="number" class="pp_in" data-line="' + ln + '" data-field="hours" value="' + (planHoursVal === '' ? '' : planHoursVal) + '" placeholder="' + (actHours != null ? pfmt.int(actHours) : '') + '"' + lockAttr + ' '
      + 'style="width:120px;padding:5px 8px;border:1px solid var(--border-2);border-radius:7px;font-family:inherit;text-align:right;font-size:12.5px' + lockStyle + '"></td>'
      + '<td class="pp-delta-hours">' + hoursDeltaCell + '</td></tr>';
    // 행3: 생산성(자동)
    html += '<tr data-line="' + ln + '" style="background:var(--bg-1,#fafbfc)">'
      + '<td style="color:var(--muted)">생산성</td>'
      + '<td class="num"><span style="color:var(--muted-2);font-size:11px">' + unit + '/일</span></td>'
      + '<td class="num" style="color:var(--muted)">' + (actProductivity != null ? pfmt.dec(actProductivity, 1) : '-') + '</td>'
      + '<td class="num pp-prdct" style="font-weight:600">' + (planProductivity != null ? pfmt.dec(planProductivity, 1) : '-') + '</td>'
      + '<td><span style="color:var(--muted-2);font-size:11px">자동</span></td></tr>';
  });

  $('#ppTbody').innerHTML = html;
  prodplanRenderKpis();
}

function prodplanRenderKpis() {
  var cur = prodplanCurrentMonth();
  var setCnt = PowerDB.lines.filter(function (ln) { return ProdPlan.isAnySet(cur, ln); }).length;
  // 제지·화장지(paper) 예상 생산량 합계(톤) + 실적 대비
  var paperActualTon = 0, paperPlanTon = 0;
  PowerDB.lines.forEach(function (ln) {
    if (PowerDB.lineType[ln] === 'paper') {
      var a = ProdPlan.actual(cur, ln, 'prod'); var p = ProdPlan.get(cur, ln, 'prod');
      paperActualTon += (a != null ? ppToDisp(ln, a) : 0);
      paperPlanTon += (p != null ? ppToDisp(ln, p) : 0);
    }
  });
  var paperDelta = paperActualTon > 0 ? ((paperPlanTon - paperActualTon) / paperActualTon * 100) : 0;
  var kp = $('#ppKpis');
  if (kp) kp.innerHTML =
    kpiCard('예상값 입력 호기', setCnt + ' / ' + PowerDB.lines.length, '개', 'fa-pen', 'ico-blue', 'delta-flat', 'fa-industry', cur.replace('-', '.') + ' 기준')
    + kpiCard('제지·화장지 예상 생산량', pfmt.int(paperPlanTon), '톤', 'fa-weight-hanging', 'ico-indigo', (paperDelta >= 0 ? 'delta-up' : 'delta-down'), (paperDelta >= 0 ? 'fa-arrow-up' : 'fa-arrow-down'), (paperDelta >= 0 ? '+' : '') + paperDelta.toFixed(1) + '% vs 실적')
    + kpiCard('실적 대비 변동(제지·화장지)', (paperDelta >= 0 ? '+' : '') + paperDelta.toFixed(1), '%', 'fa-chart-line', 'ico-amber', 'delta-flat', 'fa-bolt', '생산량 톤 기준');
  var badge = $('#pp_setbadge');
  if (badge) badge.innerHTML = (setCnt > 0 ? tag('예상값 ' + setCnt + '개 호기', 'TRANSFER') : tag('전부 실적값', 'SHIPMENT'));
}

function prodplanDownloadTemplate() {
  // 시트 구조: 호기 | 항목(생산량/가동시간) | 단위 | 2026-01 ... 2026-12  (실적값 채움)
  var header = ['호기', '항목', '단위'].concat(PowerDB.months);
  var aoa = [header];
  PowerDB.lines.forEach(function (ln) {
    var unit = ppDispUnit(ln);
    var prodRow = [ln, '생산량', unit];
    var hoursRow = [ln, '가동시간', 'Hr'];
    PowerDB.months.forEach(function (m) {
      var p = ProdPlan.actual(m, ln, 'prod');
      var h = ProdPlan.actual(m, ln, 'hours');
      prodRow.push(p != null ? Math.round(ppToDisp(ln, p)) : '');
      hoursRow.push(h != null ? h : '');
    });
    aoa.push(prodRow);
    aoa.push(hoursRow);
  });
  if (typeof XLSX === 'undefined') {
    var csv = aoa.map(function (r) { return r.join(','); }).join('\n');
    var blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a'); a.href = url; a.download = '예상생산량_가동시간_양식.csv'; a.click();
    URL.revokeObjectURL(url);
    return;
  }
  var ws = XLSX.utils.aoa_to_sheet(aoa);
  var wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, '이동계획');
  XLSX.writeFile(wb, '예상생산량_가동시간_양식.xlsx');
}

function prodplanHandleFile(file) {
  var msg = $('#pp_uploadmsg');
  if (typeof XLSX === 'undefined') {
    if (msg) msg.innerHTML = '<div class="note" style="border-color:#dc2626;color:#dc2626"><i class="fas fa-triangle-exclamation"></i> 엑셀 파서를 불러오지 못했습니다. 네트워크 확인 후 다시 시도하세요.</div>';
    return;
  }
  var reader = new FileReader();
  reader.onload = function (e) {
    try {
      var data = new Uint8Array(e.target.result);
      var wb = XLSX.read(data, { type: 'array' });
      var ws = wb.Sheets[wb.SheetNames[0]];
      var aoa = XLSX.utils.sheet_to_json(ws, { header: 1, blankrows: false });
      if (!aoa.length) throw new Error('빈 시트');
      var header = aoa[0].map(function (c) { return ('' + c).trim(); });
      // 컬럼 위치 파악: 호기(0), 항목, 그리고 월 컬럼들
      var idxLine = 0, idxItem = -1;
      var monthCols = {}; // colIndex -> 'YYYY-MM'
      header.forEach(function (h, i) {
        var hl = h.replace(/\s/g, '');
        if (i === 0) { idxLine = 0; return; }
        if (hl === '항목' || hl === 'item') { idxItem = i; return; }
        if (hl === '단위' || hl === 'unit') return;
        var norm = normMonth(h);
        if (norm) monthCols[i] = norm;
      });
      // 항목 컬럼이 없으면: 단순 호기×월(생산량) 양식으로 간주
      var rows = [];
      for (var r = 1; r < aoa.length; r++) {
        var line = ('' + (aoa[r][idxLine] || '')).trim();
        if (!line) continue;
        var item = (idxItem >= 0) ? ('' + (aoa[r][idxItem] || '')).trim().replace(/\s/g, '') : '생산량';
        var field = (item === '가동시간' || item.toLowerCase() === 'hours' || item === 'Hr') ? 'hours' : 'prod';
        Object.keys(monthCols).forEach(function (ci) {
          var v = aoa[r][ci];
          if (v === '' || v == null) return;
          var num = +('' + v).replace(/,/g, '');
          if (isNaN(num)) return;
          var rec = { month: monthCols[ci], line: line };
          if (field === 'hours') { rec.hours = num; }
          else { rec.prod = ppToInternal(line, num); } // 표시단위(톤/천개) → 내부(kg/EA)
          rows.push(rec);
        });
      }
      var res = ProdPlan.bulkSet(rows);
      if (msg) {
        msg.innerHTML = '<div class="note" style="border-color:var(--blue);color:var(--blue-700)"><i class="fas fa-circle-check"></i> 업로드 완료 — '
          + '<b>' + res.applied + '건 반영</b>' + (res.skipped > 0 ? ', ' + res.skipped + '건 건너뜀(호기/월/값 불일치)' : '') + '. 전력비 시뮬레이션에 자동 적용됩니다.</div>';
      }
      prodplanRenderTable();
    } catch (err) {
      if (msg) msg.innerHTML = '<div class="note" style="border-color:#dc2626;color:#dc2626"><i class="fas fa-triangle-exclamation"></i> 엑셀을 읽지 못했습니다: ' + (err.message || err) + '</div>';
    }
  };
  reader.readAsArrayBuffer(file);
}

// 다양한 월 표기를 'YYYY-MM'으로 정규화 ('2026-01', '2026년 1월', '2026.1', '2026/01' 등)
function normMonth(s) {
  s = ('' + s).trim();
  var m = s.match(/(\d{4})\D+(\d{1,2})/);
  if (!m) return null;
  var y = m[1], mo = ('0' + m[2]).slice(-2);
  return y + '-' + mo; // 유효성은 bulkSet에서 검증
}

AFTER.prodplan = function () {
  if (!PowerDB.hasData()) return;

  var ms = $('#pp_month');
  if (ms) ms.addEventListener('change', function () {
    ProdPlanState.month = ms.value;
    route();
  });

  var tplBtn = $('#pp_template');
  if (tplBtn) tplBtn.addEventListener('click', prodplanDownloadTemplate);

  var file = $('#pp_file');
  if (file) file.addEventListener('change', function () {
    if (Period.isActual(prodplanCurrentMonth())) { file.value = ''; return; } // 실적월 업로드 차단
    if (file.files && file.files[0]) prodplanHandleFile(file.files[0]);
    file.value = '';
  });

  var rm = $('#pp_resetmonth');
  if (rm) rm.addEventListener('click', function () {
    ProdPlan.clearMonth(prodplanCurrentMonth());
    route();
  });

  // 입력 이벤트(위임): pp_in (data-field: prod|hours)
  var tbody = $('#ppTbody');
  if (tbody) tbody.addEventListener('input', function (e) {
    var inp = e.target;
    if (!inp.classList || !inp.classList.contains('pp_in')) return;
    var ln = inp.getAttribute('data-line');
    var field = inp.getAttribute('data-field');
    var cur = prodplanCurrentMonth();
    if (Period.isActual(cur)) return; // 실적 확정월 — 입력 무시(수정 금지)
    // 저장: 생산량은 표시단위→내부단위, 가동시간은 그대로
    var storeVal = (field === 'prod') ? ppToInternal(ln, inp.value) : (inp.value === '' ? null : +inp.value);
    ProdPlan.set(cur, ln, field, storeVal);
    prodplanUpdateRow(ln);
  });

  prodplanRenderTable();
};

// 입력 중 포커스 유지: 해당 호기의 증감/생산성/KPI만 갱신 (입력 input은 건드리지 않음)
function prodplanUpdateRow(ln) {
  var cur = prodplanCurrentMonth();
  var unit = ppDispUnit(ln);
  // 해당 호기의 3개 tr 찾기
  var trs = $all('#ppTbody tr[data-line="' + ln + '"]');
  if (!trs.length) return;

  var actProdDisp = ppToDisp(ln, ProdPlan.actual(cur, ln, 'prod'));
  var actHours = ProdPlan.actual(cur, ln, 'hours');
  var planProdSet = ProdPlan.isSet(cur, ln, 'prod');
  var planHoursSet = ProdPlan.isSet(cur, ln, 'hours');
  var planProdDisp = ppToDisp(ln, ProdPlan.get(cur, ln, 'prod'));
  var planHoursVal = ProdPlan.get(cur, ln, 'hours');

  // 증감 생산량 (행1)
  var prodDelta = (planProdSet && actProdDisp) ? ((planProdDisp - actProdDisp) / actProdDisp * 100) : null;
  var c1 = trs[0] && trs[0].querySelector('.pp-delta-prod');
  if (c1) c1.innerHTML = (prodDelta != null)
    ? '<span class="tag ' + (prodDelta >= 0 ? 'tag-up' : 'tag-down') + '">' + (prodDelta >= 0 ? '+' : '') + prodDelta.toFixed(1) + '%</span>'
    : '<span style="color:var(--muted-2);font-size:11px">실적</span>';
  // 증감 가동시간 (행2)
  var hoursDelta = (planHoursSet && actHours) ? ((planHoursVal - actHours) / actHours * 100) : null;
  var c2 = trs[1] && trs[1].querySelector('.pp-delta-hours');
  if (c2) c2.innerHTML = (hoursDelta != null)
    ? '<span class="tag ' + (hoursDelta >= 0 ? 'tag-up' : 'tag-down') + '">' + (hoursDelta >= 0 ? '+' : '') + hoursDelta.toFixed(1) + '%</span>'
    : (actHours != null ? '<span style="color:var(--muted-2);font-size:11px">실적</span>' : '<span style="color:var(--muted-2);font-size:11px">-</span>');
  // 생산성 (행3)
  var prdct = (planProdDisp != null && planHoursVal != null && planHoursVal > 0) ? (planProdDisp / (planHoursVal / 24)) : null;
  var c3 = trs[2] && trs[2].querySelector('.pp-prdct');
  if (c3) c3.innerHTML = (prdct != null ? pfmt.dec(prdct, 1) : '-');

  prodplanRenderKpis();
}

/* ====================== AI 분석 어시스턴트 (slide 9) ====================== */
var AI_SUGGEST = [
  'LATEX 원가차이가 큰 이유는?',
  '폐품율을 1%p 낮추면 절감액은?',
  '5월 가격차이(Z) 합계 알려줘',
  '단가 +3% 시 톤당 원가는?'
];
function aiAnswer(q) {
  var latex = Calc.computeMaterial(Object.assign({}, DB.materials[0], { X3: DB.meta.productionTon }));
  if (q.indexOf('LATEX') >= 0) {
    return 'LATEX(2000041)의 총차이 <b>' + fmt.wonSign(latex.AA) + '원</b>은 물량차이 Y <b>' + fmt.wonSign(latex.Y) + '</b>(실적원단위 ' + fmt.dec(DB.materials[0].Q, 2) + ' < 표준 ' + fmt.dec(DB.materials[0].L, 2) + ')와 가격차이 Z <b>' + fmt.wonSign(latex.Z) + '</b>(가중단가 1,550 > 표준 1,512.9)의 합입니다. 표준 대비 단가가 높은 것이 주 원인입니다.';
  }
  if (q.indexOf('폐품') >= 0) {
    return '폐품율을 1%p 낮추면 유효 생산톤이 약 ' + fmt.ton(Math.round(DB.meta.productionTon * 0.01)) + '톤 감소하여 재료비가 비례 절감됩니다. 시뮬레이션 화면의 <b>폐품율 슬라이더</b>로 즉시 확인할 수 있습니다.';
  }
  if (q.indexOf('가격차이') >= 0 || q.indexOf('Z') >= 0) {
    var totZ = DB.materials.reduce(function (s, m) { return s + Calc.computeMaterial(Object.assign({}, m, { X3: DB.meta.productionTon })).Z; }, 0);
    return '5월 전체 가격차이(Z) 합계는 <b>' + fmt.wonSign(totZ) + '원</b>입니다. ' + (totZ < 0 ? '표준단가 대비 실제 가중단가가 낮아 원가 절감 효과가 있습니다.' : '단가 상승으로 원가가 증가했습니다.');
  }
  if (q.indexOf('톤당') >= 0 || q.indexOf('단가') >= 0) {
    return '단가 조정은 가격차이(Z)에 직접 반영됩니다. 시뮬레이션의 <b>단가 조정 슬라이더</b>를 +3%로 설정하면 톤당 원가가 실시간 재계산되어 KPI에 표시됩니다.';
  }
  return '원가 차이는 <b>물량차이(Y)</b>와 <b>가격차이(Z)</b>로 분해됩니다. 시뮬레이션 화면에서 변수를 조정하며 영향을 확인해 보세요. 더 궁금한 점을 질문해 주세요.';
}
PAGES.ai = function () {
  if (!DB.hasData()) return emptyState('분석할 원가 데이터가 없습니다. 시뮬레이션 데이터 연동 후 AI 분석을 이용할 수 있습니다.');
  var sug = AI_SUGGEST.map(function (s) { return '<div class="s" data-q="' + s + '">' + s + '</div>'; }).join('');
  return '<div class="chat-col">'
    + '<div class="card"><div class="card__head"><h3>추천 질문</h3></div><div class="card__body"><div class="suggest">' + sug + '</div></div></div>'
    + '<div class="chat-main"><div class="chat-feed" id="aiFeed">'
    + '<div class="bubble ai">안녕하세요. 사전원가 분석 어시스턴트입니다. 원가 차이의 원인이나 시뮬레이션 결과에 대해 물어보세요.</div>'
    + '</div><div class="chat-input"><input id="aiInput" type="text" placeholder="질문을 입력하세요..."><button class="btn btn-primary" id="aiSend"><i class="fas fa-paper-plane"></i></button></div></div>'
    + '<div class="card"><div class="card__head"><h3>분석 컨텍스트</h3></div><div class="card__body">'
    + miniStat('대상', 'PM2 · 5월', '') + miniStat('지종', 'SC · 220g/m²', '') + miniStat('생산', fmt.ton(DB.meta.productionTon) + '톤', '')
    + miniStat('총차이', fmt.wonSign(DB.materials.reduce(function (s, m) { return s + Calc.computeMaterial(Object.assign({}, m, { X3: DB.meta.productionTon })).AA; }, 0) / 1000000) + 'M', '') + '</div></div>'
    + '</div>';
};
function aiPush(text, who) {
  var feed = $('#aiFeed');
  feed.appendChild(el('<div class="bubble ' + who + '">' + text + '</div>'));
  feed.scrollTop = feed.scrollHeight;
}
function aiAsk(q) {
  if (!q) return;
  aiPush(q, 'user');
  setTimeout(function () { aiPush(aiAnswer(q), 'ai'); }, 350);
}
AFTER.ai = function () {
  if (!DB.hasData()) return;
  var inp = $('#aiInput');
  $('#aiSend').addEventListener('click', function () { var v = inp.value.trim(); inp.value = ''; aiAsk(v); });
  inp.addEventListener('keydown', function (e) { if (e.key === 'Enter') { var v = inp.value.trim(); inp.value = ''; aiAsk(v); } });
  $all('.suggest .s').forEach(function (s) { s.addEventListener('click', function () { aiAsk(s.getAttribute('data-q')); }); });
};

/* ====================== INIT ====================== */
renderShell();
route();
window.addEventListener('hashchange', route);
