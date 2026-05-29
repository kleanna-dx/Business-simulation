package com.company.precost.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 시스템 공통 에러코드.
 * BUSINESS_xxx : 비즈니스 규칙 위반 (4xx)
 * SYSTEM_xxx   : 시스템/인프라 오류 (5xx)
 */
@Getter
public enum ErrorCode {

    // --- 공통 ---
    INVALID_INPUT("BUSINESS_001", HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    ENTITY_NOT_FOUND("BUSINESS_002", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_ENTITY("BUSINESS_003", HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),
    INVALID_STATE("BUSINESS_004", HttpStatus.CONFLICT, "현재 상태에서 수행할 수 없는 작업입니다."),
    ACCESS_DENIED("BUSINESS_005", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHENTICATED("BUSINESS_006", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // --- 인증/인가 ---
    LOGIN_FAILED("BUSINESS_010", HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED("BUSINESS_011", HttpStatus.FORBIDDEN, "계정이 잠겼습니다. 잠시 후 다시 시도하세요."),
    TOKEN_EXPIRED("BUSINESS_012", HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN("BUSINESS_013", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    PASSWORD_POLICY_VIOLATION("BUSINESS_014", HttpStatus.BAD_REQUEST, "비밀번호 정책을 위반했습니다."),

    // --- 사전원가 도메인 ---
    INVALID_MATERIAL_CODE("BUSINESS_020", HttpStatus.BAD_REQUEST, "유효하지 않은 자재코드입니다."),
    INVALID_TARGET_MONTH("BUSINESS_021", HttpStatus.BAD_REQUEST, "기준월 형식이 올바르지 않습니다(YYYY-MM)."),
    GRADE_BREAKDOWN_MISMATCH("BUSINESS_022", HttpStatus.BAD_REQUEST, "원지구분 합계가 생산량과 일치하지 않습니다."),
    MOVEMENT_VALIDATION_FAILED("BUSINESS_023", HttpStatus.BAD_REQUEST, "이동 검증 규칙을 위반했습니다."),
    SIMULATION_PRECONDITION_NOT_MET("BUSINESS_024", HttpStatus.CONFLICT, "시뮬레이션 선행 데이터가 준비되지 않았습니다."),

    // --- SAP 연동 ---
    EXCEL_PARSE_ERROR("BUSINESS_030", HttpStatus.BAD_REQUEST, "엑셀 파싱 중 오류가 발생했습니다."),
    BW_VALIDATION_FAILED("BUSINESS_031", HttpStatus.BAD_REQUEST, "BW 데이터 검증에 실패했습니다."),

    // --- 시스템 ---
    INTERNAL_ERROR("SYSTEM_001", HttpStatus.INTERNAL_SERVER_ERROR, "내부 시스템 오류가 발생했습니다."),
    EXTERNAL_API_ERROR("SYSTEM_002", HttpStatus.BAD_GATEWAY, "외부 시스템 연동 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
