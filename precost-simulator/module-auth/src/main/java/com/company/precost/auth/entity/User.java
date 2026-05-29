package com.company.precost.auth.entity;

import com.company.precost.common.entity.BaseEntity;
import com.company.precost.domain.enums.RoleType;
import com.company.precost.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시스템 사용자. (모듈 01 인증/인가)
 * <p>비밀번호는 BCrypt 해시로 저장하며, 평문은 어떤 경우에도 보관하지 않는다.</p>
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "UK_USERS_USERNAME", columnNames = "username"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "password_hash", length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 50, nullable = false)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "department", length = 50)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private UserStatus status;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Builder
    private User(String username, String passwordHash, String fullName, String email,
                 String department, RoleType role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.failedLoginCount = 0;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /* ===================== 도메인 행위 ===================== */

    /** 로그인 성공 처리: 실패 카운트 초기화 + 최종 로그인 시각 갱신. */
    public void onLoginSuccess() {
        this.failedLoginCount = 0;
        this.lastLoginAt = LocalDateTime.now();
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }

    /** 로그인 실패 처리: 임계치(maxAttempts) 도달 시 계정 잠금. */
    public void onLoginFailure(int maxAttempts) {
        this.failedLoginCount++;
        if (this.failedLoginCount >= maxAttempts) {
            this.status = UserStatus.LOCKED;
        }
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
        this.failedLoginCount = 0;
    }

    public void changeRole(RoleType role) {
        this.role = role;
    }

    public void lock()     { this.status = UserStatus.LOCKED; }
    public void unlock()   { this.status = UserStatus.ACTIVE; this.failedLoginCount = 0; }
    public void inactivate() { this.status = UserStatus.INACTIVE; }
    public void activate()   { this.status = UserStatus.ACTIVE; }

    public boolean isActive() { return this.status == UserStatus.ACTIVE; }
    public boolean isLocked() { return this.status == UserStatus.LOCKED; }
}
