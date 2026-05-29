package com.company.precost.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing 활성화 + 현재 사용자(created_by/updated_by) 추출 설정.
 * app-api 의 메인 클래스에서 자동 import 되도록 com.company.precost 패키지 하위에 위치.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * 현재 인증된 사용자명을 created_by/updated_by 에 주입한다.
     * 인증 정보가 없으면 "system" 을 사용한다(배치/초기 적재 등).
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }
            return Optional.of(authentication.getName());
        };
    }
}
