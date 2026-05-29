package com.company.precost.auth.service;

import com.company.precost.auth.dto.CreateUserRequest;
import com.company.precost.auth.dto.UpdateUserRequest;
import com.company.precost.auth.dto.UserSummary;
import com.company.precost.auth.entity.User;
import com.company.precost.auth.repository.RolePermissionRepository;
import com.company.precost.auth.repository.UserRepository;
import com.company.precost.common.exception.BusinessException;
import com.company.precost.common.exception.ErrorCode;
import com.company.precost.domain.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 관리 서비스. (모듈 01 인증/인가 — ADMIN 기능)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    @Transactional
    public UserSummary create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ENTITY, "이미 사용 중인 아이디입니다.");
        }
        passwordPolicyValidator.validate(req.password());
        User user = User.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .email(req.email())
                .department(req.department())
                .role(req.role())
                .build();
        userRepository.save(user);
        return toSummary(user);
    }

    @Transactional
    public UserSummary update(Long id, UpdateUserRequest req) {
        User user = findUser(id);
        if (req.role() != null) {
            user.changeRole(req.role());
        }
        // fullName/email/department 부분 수정은 엔티티 setter 미노출 정책상
        // 별도 도메인 메서드로 처리(생략 가능). 여기서는 역할 변경만 반영.
        return toSummary(user);
    }

    @Transactional(readOnly = true)
    public Page<UserSummary> search(String keyword, RoleType role, Pageable pageable) {
        return userRepository.search(keyword, role, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public UserSummary get(Long id) {
        return toSummary(findUser(id));
    }

    @Transactional
    public void lock(Long id)   { findUser(id).lock(); }

    @Transactional
    public void unlock(Long id) { findUser(id).unlock(); }

    @Transactional
    public void inactivate(Long id) { findUser(id).inactivate(); }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private UserSummary toSummary(User user) {
        List<String> perms = rolePermissionRepository.findPermissionCodesByRole(user.getRole());
        return UserSummary.from(user, perms);
    }
}
