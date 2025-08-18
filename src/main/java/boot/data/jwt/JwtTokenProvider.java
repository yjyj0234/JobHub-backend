package boot.data.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import boot.data.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyRaw;

    @Value("${jwt.expiration:21600000}") // 기본 6시간(6*60*60*1000) = 21,600,000ms
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        // secret이 "평문"이라면 그냥 bytes로, 이미 Base64라면 decode해서 쓰면 됨.
        // 지금은 평문 가정 (Base64 인코딩을 다시 하지 않음)
        this.key = Keys.hmacShaKeyFor(secretKeyRaw.getBytes(StandardCharsets.UTF_8));
    }

    // userId를 uid로, 이메일은 subject로, role은 claim으로 넣기
    public String createToken(Long userId, String email, String role) {
        String normalized = (role != null && role.startsWith("ROLE_"))
            ? role.substring(5)
            : role;
    
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
    
        return Jwts.builder()
                .setSubject(email)
                .claim("uid", userId)
                .claim("role", normalized) // ← 접두어 없이 저장
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        Number n = getClaims(token).get("uid", Number.class);
        return n == null ? null : n.longValue();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // private String normalizeRole(String role) {
    //     if (role == null || role.isBlank()) return null;
    //     return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    // }

    
    // 필요하다면 여기서 Authentication 생성(Principal을 uid로 세팅)
   // 인증 객체 생성 시도: 권한(authority)에도 접두어 없이 그대로 세팅
public Authentication getAuthentication(String token) {
    Long uid = getUserId(token);
    String email = getEmail(token);

    String roleClaim = getRole(token); // "USER" or "ROLE_USER" (과거 토큰 호환)
    // 혹시 이전 토큰에 ROLE_로 들어있으면 제거
    String authority = (roleClaim != null && roleClaim.startsWith("ROLE_"))
        ? roleClaim.substring(5)
        : roleClaim; // 최종: "USER"/"COMPANY"/"ADMIN"

    var authorities = (authority == null)
        ? java.util.List.<SimpleGrantedAuthority>of()
        : java.util.List.of(new SimpleGrantedAuthority(authority)); // ← 접두어 없음

    // AuthUser의 세 번째 파라미터는 문자열 "USER" 그대로 사용(또는 enum이면 enum으로)
    var principal = new AuthUser(uid, email, authority);

    var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    System.out.println("[JWT] set auth uid=" + uid + " authorities=" + auth.getAuthorities());
    return auth;
}
}
