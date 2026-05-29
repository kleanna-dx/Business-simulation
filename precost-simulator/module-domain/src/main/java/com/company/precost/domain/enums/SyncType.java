package com.company.precost.domain.enums;

/**
 * SAP 동기화 유형.
 */
public enum SyncType {
    BATCH,        // 월배치 자동
    MANUAL,       // 수동 트리거
    EXCEL_UPLOAD  // 엑셀 업로드 폴백
}
