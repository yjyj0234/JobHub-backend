package boot.data.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// ⭐ 추가
import org.springframework.util.AntPathMatcher;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // ⭐ 추가: 공개(permitAll) 경로 패턴 — SecurityConfig와 일치시키기
    private static final List<String> PUBLIC_PATTERNS = List.of(
        "/api/search/**",
        "/auth/**",
        "/public/**",
        "/jobpostinglist/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ⭐ 공개 경로는 바로 패스(토큰 검사 자체를 하지 않음)
        String uri = request.getRequestURI();
        if (isPublic(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        try {
            token = resolveToken(request); // 기존 로직 유지
        } catch (Exception ignored) {
            // ⭐ 토큰 파싱 중 예외가 나도 여기서 막지 않고 통과시킴
        }

        try {
            // 1) Authorization 헤더 우선
            // 2) 쿠키에서 JWT 찾기
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // ⭐ token == null 이거나 validate 실패여도 여기서 401/403 반출하지 않음.
            //    인가가 필요한 엔드포인트라면 이후 Security가 처리.
        } catch (Exception ignored) {
            // ⭐ 검증/인증 중 예외도 여기서 삼키고 체인 진행
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1) Authorization 헤더 우선
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        // 2) 쿠키에서 JWT 찾기
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    // ⭐ 추가: 공개 경로 매칭
    private boolean isPublic(String uri) {
        for (String pattern : PUBLIC_PATTERNS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
