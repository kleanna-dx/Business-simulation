package com.company.precost.api;

import com.company.precost.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 헬스/버전 확인용 공개 엔드포인트. (app-api)
 */
@Tag(name = "System", description = "시스템 상태 API")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Operation(summary = "헬스 체크")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "application", "precost-simulator",
                "time", LocalDateTime.now().toString()
        ));
    }
}
