package com.company.precost.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.Map;

/**
 * 모든 REST API 응답을 감싸는 표준 래퍼.
 * 형식: { "data": T, "meta": {}, "error": null }
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private final T data;
    private final Map<String, Object> meta;
    private final ErrorBody error;

    private ApiResponse(T data, Map<String, Object> meta, ErrorBody error) {
        this.data = data;
        this.meta = meta;
        this.error = error;
    }

    /** 성공 응답 (200) */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, Map.of(), null);
    }

    /** 성공 응답 + meta */
    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(data, meta, null);
    }

    /** 생성 성공 응답 (201) */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(data, Map.of("created", true), null);
    }

    /** 에러 응답 */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(null, Map.of(), new ErrorBody(code, message));
    }

    /**
     * 에러 본문.
     */
    @Getter
    public static class ErrorBody {
        private final String code;
        private final String message;

        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
