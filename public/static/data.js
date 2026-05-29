/* ============================================================
   사전원가 시뮬레이션 — 데이터 계층
   ------------------------------------------------------------
   DEMO_MODE : 데모(가상)데이터 표시 여부
     - 자동 판정: localhost / sandbox 도메인 또는 ?demo=1  -> 데모 ON
                  그 외(실제 운영 도메인)               -> 데모 OFF(빈 상태)
     - 강제 OFF: URL 에 ?demo=0  (운영 도메인 미리보기 검증용)
   운영 배포 시에는 빈 스키마로 시작하며, DB.load(payload)로 API 데이터를 주입합니다.
   ============================================================ */

var DEMO_MODE = (function () {
  try {
    var qs = new URLSearchParams(location.search);
    if (qs.get('demo') === '1') return true;
    if (qs.get('demo') === '0') return false;
    var h = location.hostname || '';
    // 개발/미리보기 환경에서만 자동 데모
    return (h === 'localhost' || h === '127.0.0.1' || h === '' ||
            h.indexOf('sandbox') >= 0 || h.indexOf('e2b') >= 0 ||
            h.indexOf('novita') >= 0);
  } catch (e) { return false; }
})();

/* ---------- 빈 스키마 (운영 기본값) ---------- */
function emptyDB() {
  return {
    meta: {
      plant: '', plantNm: '-', month: '', monthLabel: '-',
      grade: '-', gradeNm: '-', basisWeight: 0, productionTon: 0, defectRate: 0,
      baseMonth: '', user: '-', role: '', roleNm: '-'
    },
    materials: [],
    movements: [],
    plans: [],
    bwRows: [],
    approvals: [],
    trend: { labels: [], PM2: [], PM3: [] },
    alerts: []
  };
}

/* ---------- 데모(가상) 데이터 ---------- */
var DEMO_DATA = {
  meta: {
    plant: 'PM2', plantNm: '전주 2호기', month: '2026-05', monthLabel: '2026년 5월',
    grade: 'SC', gradeNm: '백상지(SC)', basisWeight: 220, productionTon: 16000, defectRate: 1.5,
    baseMonth: '2026-04', user: '김원가', role: 'COST_MANAGER', roleNm: '원가담당자'
  },
  materials: [
    { code:'2000041', name:'LATEX',   maker:'금호석유',   group:'KSL',
      L:5.3643, M:1512.90, N:121819, Q:5.20, R:8200, S:1550, T:2000, U:1550 },
    { code:'2000057', name:'BOOSTER', maker:'한솔케미칼', group:'KSL',
      L:1.2841, M:980.50,  N:18420,  Q:1.31, R:5400, S:990,  T:1500, U:990  },
    { code:'2000003', name:'CLAY',    maker:'KOMINE',     group:'PIG',
      L:42.115, M:142.30,  N:95870,  Q:41.80, R:30000, S:144, T:9000, U:144 }
  ],
  movements: [
    { id:'MV-2605-001', type:'입고', typeKey:'INBOUND',  mat:'2000041', matNm:'LATEX',   from:'금호석유',   to:'PM2-원료창고', plan:8200, actual:8180, price:1550, date:'2026-05-03', status:'완료',  statusKey:'DONE' },
    { id:'MV-2605-002', type:'입고', typeKey:'INBOUND',  mat:'2000057', matNm:'BOOSTER', from:'한솔케미칼', to:'PM2-원료창고', plan:1500, actual:1500, price:990,  date:'2026-05-04', status:'완료',  statusKey:'DONE' },
    { id:'MV-2605-003', type:'출고', typeKey:'OUTBOUND', mat:'2000041', matNm:'LATEX',   from:'PM2-원료창고', to:'PM2-생산라인', plan:5200, actual:5180, price:1550, date:'2026-05-08', status:'완료',  statusKey:'DONE' },
    { id:'MV-2605-004', type:'이송', typeKey:'TRANSFER', mat:'2000003', matNm:'CLAY',    from:'PM3-창고',     to:'PM2-원료창고', plan:9000, actual:9000, price:144,  date:'2026-05-10', status:'진행중',statusKey:'IN_PROGRESS' },
    { id:'MV-2605-005', type:'조정', typeKey:'ADJUST',   mat:'2000057', matNm:'BOOSTER', from:'재고실사',    to:'PM2-원료창고', plan:0,    actual:-25, price:990,  date:'2026-05-12', status:'완료',  statusKey:'DONE' },
    { id:'MV-2605-006', type:'출하', typeKey:'SHIPMENT', mat:'2000003', matNm:'CLAY',    from:'PM2-원료창고', to:'PM4-창고',     plan:1200, actual:1180, price:144,  date:'2026-05-15', status:'예정',  statusKey:'PLANNED' },
    { id:'MV-2605-007', type:'반품', typeKey:'RETURN',   mat:'2000041', matNm:'LATEX',   from:'PM2-원료창고', to:'금호석유',     plan:0,    actual:40,  price:1550, date:'2026-05-18', status:'예정',  statusKey:'PLANNED' }
  ],
  plans: [
    { id:'PL-2605-PM2', plant:'PM2', grade:'SC', bw:220, ton:16000, status:'진행중', statusKey:'IN_PROGRESS', owner:'김원가', updated:'2026-05-20' },
    { id:'PL-2605-PM3', plant:'PM3', grade:'NP', bw:80,  ton:22000, status:'완료',  statusKey:'DONE',        owner:'박생산', updated:'2026-05-18' },
    { id:'PL-2605-PM4', plant:'PM4', grade:'KP', bw:120, ton:9500,  status:'초안',  statusKey:'DRAFT',       owner:'이계획', updated:'2026-05-21' }
  ],
  bwRows: [
    { code:'2000041', name:'LATEX',   field:'표준원단위(L)', value:5.3643,  src:'SAP', recv:'2026-05-01 06:12' },
    { code:'2000041', name:'LATEX',   field:'표준단가(M)',   value:1512.90, src:'SAP', recv:'2026-05-01 06:12' },
    { code:'2000057', name:'BOOSTER', field:'표준원단위(L)', value:1.2841,  src:'SAP', recv:'2026-05-01 06:12' },
    { code:'2000003', name:'CLAY',    field:'표준단가(M)',   value:142.30,  src:'SAP', recv:'2026-05-01 06:12' },
    { code:'-',       name:'생산계획', field:'생산량(톤)',   value:16000,   src:'SAP', recv:'2026-05-01 06:14' }
  ],
  approvals: [
    { id:'AP-2605-01', title:'PM2 5월 사전원가 확정',  amount:8924,  requester:'김원가', step:'팀장 승인',  status:'상신',  statusKey:'SUBMITTED', date:'2026-05-22' },
    { id:'AP-2605-02', title:'PM3 5월 사전원가 확정',  amount:6310,  requester:'박생산', step:'완료',      status:'승인',  statusKey:'APPROVED',  date:'2026-05-19' },
    { id:'AP-2605-03', title:'PM4 LATEX 단가 변경 반영', amount:240,   requester:'이계획', step:'담당 검토', status:'반려',  statusKey:'REJECTED',  date:'2026-05-21' },
    { id:'AP-2605-04', title:'PM2 CLAY 이송 정산',     amount:130,   requester:'최물류', step:'초안',      status:'초안',  statusKey:'DRAFT',     date:'2026-05-23' }
  ],
  trend: {
    labels:['1월','2월','3월','4월','5월'],
    PM2:[8420, 8510, 8390, 8680, 8924],
    PM3:[6020, 6110, 6240, 6180, 6310]
  },
  alerts: [
    { sev:'high', icon:'fa-triangle-exclamation', title:'LATEX 단가 차이 +2.4% 초과',     meta:'PM2 · 임계치 ±2.0% · 5월 22일' },
    { sev:'warn', icon:'fa-clock',                title:'PM2 사전원가 승인 대기 2건',      meta:'팀장 승인 단계 · 마감 D-3' },
    { sev:'info', icon:'fa-rotate',               title:'SAP BW 5월분 수신 완료',          meta:'12개 자재 · 2026-05-01 06:14' }
  ]
};

/* ---------- DB 초기화 ---------- */
/* 데모 모드면 데모 데이터, 운영이면 빈 스키마로 시작 */
var DB = DEMO_MODE ? JSON.parse(JSON.stringify(DEMO_DATA)) : emptyDB();

/* 데이터 존재 여부 (Empty State 판정용) */
DB.hasData = function () {
  return (this.materials && this.materials.length > 0);
};

/* API 연동 자리: 운영 배포 후 Spring Boot 응답으로 DB를 채울 때 사용
   사용 예) DB.load(await (await fetch('/api/precost/2026-05?plant=PM2')).json());
   payload 형태는 DEMO_DATA 와 동일한 구조를 기대합니다. */
DB.load = function (payload) {
  if (!payload) return this;
  var self = this;
  ['meta','materials','movements','plans','bwRows','approvals','trend','alerts'].forEach(function (k) {
    if (payload[k] != null) self[k] = payload[k];
  });
  return this;
};

/* 상태/타입 → 태그 클래스 매핑 */
var TAGCLS = {
  DONE:'tag-up', PROGRESS:'tag-blue', PLANNED:'tag-gray', IN_PROGRESS:'tag-blue',
  SUBMITTED:'tag-amber', APPROVED:'tag-up', DRAFT:'tag-gray', REJECTED:'tag-down',
  INBOUND:'tag-up', OUTBOUND:'tag-blue', TRANSFER:'tag-gray', ADJUST:'tag-amber',
  SHIPMENT:'tag-blue', RETURN:'tag-down'
};
