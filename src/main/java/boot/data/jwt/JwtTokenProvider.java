package boot.data.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import boot.data.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyRaw; // application.yml

    @Value("${jwt.expiration:21600000}") // 6시간
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
protected void init() {
    String raw = (secretKeyRaw == null) ? "" : secretKeyRaw.trim();

    byte[] keyBytes;
    try {
        keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(raw); // Base64로 해석 가능하면 그걸로
    } catch (IllegalArgumentException ignore) {
        keyBytes = raw.getBytes(StandardCharsets.UTF_8);           // 아니면 평문
    }

    if (keyBytes.length < 32) { // HS256 최소 길이 보장
        byte[] padded = new byte[32];
        System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
        for (int i = keyBytes.length; i < 32; i++) padded[i] = (byte) (i * 31 + 7);
        keyBytes = padded;
    }

    this.key = Keys.hmacShaKeyFor(keyBytes);

    // ✅ 길이와 앞 12바이트 Base64로 로그 (키 노출 방지)
    String head = java.util.Base64.getEncoder().encodeToString(
        java.util.Arrays.copyOf(keyBytes, Math.min(12, keyBytes.length))
    );
    System.out.println("[JWT] key initialized. size=" + keyBytes.length + " head(b64)=" + head + "...");
}

    // 토큰 생성
    public String createToken(Long userId, String email, String role) {
        String normalized = normalizeRole(role); // "USER"/"ADMIN"/"COMPANY" 형태로 저장
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setSubject(email)
                .claim("uid", userId)
                .claim("role", normalized)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();

        return token;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return null;
        String r = role.trim();
        if (r.startsWith("ROLE_")) r = r.substring(5);
        return r.toUpperCase(); // "user" → "USER"
    }

    public boolean validateToken(String token) {
        try {
            var jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            String alg = String.valueOf(jws.getHeader().getAlgorithm());
            System.out.println("[JWT] validate OK alg=" + alg + " exp=" + jws.getBody().getExpiration());
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("[JWT] validateToken failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    public String getEmail(String token) { return getClaims(token).getSubject(); }

    public Long getUserId(String token) {
        Number n = getClaims(token).get("uid", Number.class);
        return n == null ? null : n.longValue();
    }

    public String getRole(String token) { return getClaims(token).get("role", String.class); }

    public Authentication getAuthentication(String token) {
        Long uid = getUserId(token);
        String email = getEmail(token);

        String roleClaim = getRole(token); // "USER" 또는 과거 "ROLE_USER"
        String authority = normalizeRole(roleClaim); // 최종 "USER"

        List<SimpleGrantedAuthority> authorities =
                (authority == null) ? List.of() : List.of(new SimpleGrantedAuthority(authority));

        var principal = new AuthUser(uid, email, authority);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        System.out.println("[JWT] auth SET -> uid=" + uid + ", email=" + email + ", authorities=" + authorities);
        return auth;
    }
}
