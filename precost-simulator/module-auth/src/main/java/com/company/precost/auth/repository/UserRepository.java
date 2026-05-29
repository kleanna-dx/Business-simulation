package com.company.precost.auth.repository;

import com.company.precost.auth.entity.User;
import com.company.precost.domain.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소. (모듈 01 인증/인가)
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(RoleType role);

    @Query("""
            select u from User u
            where (:keyword is null
                   or u.username like concat('%', :keyword, '%')
                   or u.fullName like concat('%', :keyword, '%'))
              and (:role is null or u.role = :role)
            """)
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") RoleType role,
                      Pageable pageable);
}
