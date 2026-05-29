package com.company.precost.auth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 프로퍼티. (application.yml의 {@code security.jwt.*})
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** HMAC-SHA256 서명 비밀키 (Base64 또는 평문 32바이트 이상). */
    private String secret;

    /** Access Token 만료(ms). 기본 30분. */
    private long accessTokenValidityMs = 30 * 60 * 1000L;

    /** Refresh Token 만료(ms). 기본 7일. */
    private long refreshTokenValidityMs = 7 * 24 * 60 * 60 * 1000L;

    /** 토큰 발급자(iss). */
    private String issuer = "precost-simulator";
}
