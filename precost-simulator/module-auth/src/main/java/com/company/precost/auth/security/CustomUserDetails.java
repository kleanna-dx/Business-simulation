package com.company.precost.auth.security;

import com.company.precost.auth.entity.User;
import com.company.precost.domain.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} 어댑터. (모듈 01 인증/인가)
 * <p>역할(ROLE_*) + 세부 권한(permission code) 을 authority 로 노출한다.</p>
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final UserStatus status;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user, List<String> permissionCodes) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.status = user.getStatus();
        this.authorities = new ArrayList<>();
        this.authorities.add(new SimpleGrantedAuthority(user.getRole().authority()));
        if (permissionCodes != null) {
            permissionCodes.forEach(c -> authorities.add(new SimpleGrantedAuthority(c)));
        }
    }

    /** JWT 클레임으로부터 직접 구성(필터에서 사용, DB 조회 없음). */
    public CustomUserDetails(Long userId, String username, String role, List<String> permissionCodes) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = null;
        this.status = UserStatus.ACTIVE;
        this.authorities = new ArrayList<>();
        this.authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
        if (permissionCodes != null) {
            permissionCodes.forEach(c -> authorities.add(new SimpleGrantedAuthority(c)));
        }
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return status != UserStatus.LOCKED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return status == UserStatus.ACTIVE; }
}
