package boot.data.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}") // 최소 32바이트 이상(HS256)
    private String secretKeyRaw;

    private final long validityInMilliseconds = 36000000;

    JwtTokenProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    } // 10시간

    private SecretKey key() {
        // secret이 그냥 문자열이면 아래처럼 사용 (Base64 미필요)
        return Keys.hmacShaKeyFor(secretKeyRaw.getBytes(StandardCharsets.UTF_8));
        // 만약 Base64로 저장했다면: Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyRaw));
    }

    public String createToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();
        return new UsernamePasswordAuthenticationToken(new User(email, "", List.of()), "", List.of());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
