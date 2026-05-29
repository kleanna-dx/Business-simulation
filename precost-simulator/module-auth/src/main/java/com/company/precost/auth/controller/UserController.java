package com.company.precost.auth.controller;

import com.company.precost.auth.dto.CreateUserRequest;
import com.company.precost.auth.dto.UpdateUserRequest;
import com.company.precost.auth.dto.UserSummary;
import com.company.precost.auth.service.UserService;
import com.company.precost.common.response.ApiResponse;
import com.company.precost.common.response.PageResponse;
import com.company.precost.domain.enums.RoleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 API — ADMIN 전용. (모듈 01 인증/인가)
 * <pre>
 *   POST   /api/v1/users           사용자 생성
 *   GET    /api/v1/users           사용자 검색(페이징)
 *   GET    /api/v1/users/{id}      사용자 단건
 *   PUT    /api/v1/users/{id}      사용자 수정
 *   POST   /api/v1/users/{id}/lock|unlock|inactivate  상태 변경
 * </pre>
 */
@Tag(name = "User", description = "사용자 관리 API (ADMIN)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성")
    @PostMapping
    public ApiResponse<UserSummary> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.created(userService.create(request));
    }

    @Operation(summary = "사용자 검색")
    @GetMapping
    public ApiResponse<PageResponse<UserSummary>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoleType role,
            Pageable pageable) {
        return ApiResponse.success(PageResponse.of(userService.search(keyword, role, pageable)));
    }

    @Operation(summary = "사용자 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<UserSummary> get(@PathVariable Long id) {
        return ApiResponse.success(userService.get(id));
    }

    @Operation(summary = "사용자 수정")
    @PutMapping("/{id}")
    public ApiResponse<UserSummary> update(@PathVariable Long id,
                                           @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @Operation(summary = "계정 잠금")
    @PostMapping("/{id}/lock")
    public ApiResponse<Void> lock(@PathVariable Long id) {
        userService.lock(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "계정 잠금 해제")
    @PostMapping("/{id}/unlock")
    public ApiResponse<Void> unlock(@PathVariable Long id) {
        userService.unlock(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "계정 비활성화")
    @PostMapping("/{id}/inactivate")
    public ApiResponse<Void> inactivate(@PathVariable Long id) {
        userService.inactivate(id);
        return ApiResponse.success(null);
    }
}
