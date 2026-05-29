package com.company.precost.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성/검증 컴포넌트. (jjwt 0.12.x)
 * <p>Stateless 인증의 핵심 — 서버는 세션을 보관하지 않는다.</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = resolveKey(props.getSecret());
    }

    private SecretKey resolveKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret 가 설정되지 않았습니다.");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
            if (bytes.length < 32) {
                bytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /** Access Token 발급. subject=username, claims=userId/role/permissions. */
    public String createAccessToken(Long userId, String username, String role, List<String> permissions) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getAccessTokenValidityMs());
        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(username)
                .claim("uid", userId)
                .claim("role", role)
                .claim("perms", permissions)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /** Refresh Token 발급(권한 클레임 제외). */
    public String createRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getRefreshTokenValidityMs());
        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(username)
                .claim("uid", userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /** 토큰 검증 후 Claims 반환. 유효하지 않으면 null. */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(props.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 검증 실패: {}", e.getMessage());
            return null;
        }
    }

    public boolean isValid(String token) {
        return parse(token) != null;
    }

    public long getAccessTokenValiditySeconds() {
        return props.getAccessTokenValidityMs() / 1000;
    }
}
