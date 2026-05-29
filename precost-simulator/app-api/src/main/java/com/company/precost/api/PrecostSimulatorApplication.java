package com.company.precost.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 사전원가 시뮬레이션 시스템 메인 애플리케이션. (app-api)
 * <p>멀티모듈 구조이므로 컴포넌트/엔티티/리포지토리 스캔 경로를 루트 패키지로 확장한다.</p>
 */
@SpringBootApplication(scanBasePackages = "com.company.precost")
@EnableJpaRepositories(basePackages = "com.company.precost")
@EntityScan(basePackages = "com.company.precost")
public class PrecostSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrecostSimulatorApplication.class, args);
    }
}
