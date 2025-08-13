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
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)               // 표시용/로그용
                .claim("uid", userId)            // 프론트/백에서 꺼내 쓸 사용자 PK
                .claim("role", role)             // 권한(단수). 여러개면 roles(List)로!
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

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return null;
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
    // 필요하다면 여기서 Authentication 생성(Principal을 uid로 세팅)
    public Authentication getAuthentication(String token) {
        Long uid = getUserId(token);
        String email = getEmail(token);
        String role = normalizeRole(getRole(token)); // <- 정규화
    
        var authorities = (role == null)
            ? java.util.List.<SimpleGrantedAuthority>of()
            : java.util.List.of(new SimpleGrantedAuthority(role));
    
        // principal 을 AuthUser 로
        var principal = new AuthUser(uid, email, role);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
