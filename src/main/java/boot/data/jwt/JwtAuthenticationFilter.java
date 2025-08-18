package boot.data.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * CORS 프리플라이트 등 굳이 검사할 필요 없는 요청은 스킵
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) return true; // preflight
        // 필요하다면 공개 경로도 스킵 가능(선택)
        // if (PATH_MATCHER.match("/auth/**", request.getRequestURI())) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            final String token = resolveToken(request);

            if (token == null || token.isBlank()) {
                // 토큰이 없으면 그냥 통과 (익명 접근은 이후 Security 설정이 처리)
                chain.doFilter(request, response);
                return;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                // 유효하지 않은 토큰: 엔트리포인트에서 401을 만들 수 있도록 체인 진행
                request.setAttribute("authError", "INVALID_JWT");
                log.debug("[JWT] invalid token for path={}", request.getRequestURI());
                chain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("[JWT] authentication set: name={}, authorities={}",
                            auth.getName(), auth.getAuthorities());
                } else {
                    // 토큰은 유효했지만 인증 객체를 만들지 못한 경우
                    request.setAttribute("authError", "AUTH_BUILD_FAILED");
                    log.debug("[JWT] authentication object is null (valid token but no auth)");
                }
            } else {
                log.trace("[JWT] SecurityContext already has authentication; skipping set");
            }
        } catch (Exception e) {
            // 토큰 파싱/검증 중 예외가 나도 글로벌 핸들러에게 맡기고 체인 진행
            request.setAttribute("authError", e.getClass().getSimpleName());
            log.debug("[JWT] exception during filter: {} on path={}", e.toString(), request.getRequestURI());
        }

        chain.doFilter(request, response);
    }

    /**
     * Authorization 헤더(Bearer) 우선, 없으면 쿠키(JWT)에서 추출
     */
    private String resolveToken(HttpServletRequest request) {
        // 1) Authorization: Bearer <token> (대소문자 유연)
        String authz = request.getHeader("Authorization");
        if (authz != null) {
            int idx = authz.indexOf(' ');
            if (idx > 0) {
                String scheme = authz.substring(0, idx).trim();
                if ("Bearer".equalsIgnoreCase(scheme)) {
                    return authz.substring(idx + 1).trim();
                }
            }
        }
        // 2) HttpOnly 쿠키 "JWT"
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JWT".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
