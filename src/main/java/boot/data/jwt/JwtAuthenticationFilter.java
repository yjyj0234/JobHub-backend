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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // preflight
        // 공개 경로 스킵 원하면 여기에 추가
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String uri = req.getRequestURI();
        try {
            final String token = resolveToken(req);

            if (token == null || token.isBlank()) {
                log.debug("[JWT] no token on {}", uri);
                chain.doFilter(req, res);
                return;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                req.setAttribute("authError", "INVALID_JWT");
                log.debug("[JWT] invalid token on {}", uri);
                chain.doFilter(req, res);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
<<<<<<< HEAD
                    log.debug("[JWT] authentication set: name={}, authorities={}",
                            auth.getName(), auth.getAuthorities());
=======
                    log.debug("[JWT] authentication SET on {} -> name={}, authorities={}",
                              uri, auth.getName(), auth.getAuthorities());
>>>>>>> c4f32858c050bf198c87629cea13e5d7433495ed
                } else {
                    req.setAttribute("authError", "AUTH_BUILD_FAILED");
                    log.debug("[JWT] valid token but failed to build Authentication on {}", uri);
                }
            } else {
                log.trace("[JWT] context already has authentication on {}", uri);
            }
        } catch (Exception e) {
            req.setAttribute("authError", e.getClass().getSimpleName());
            log.debug("[JWT] exception {} on {}", e.toString(), uri);
        }

        chain.doFilter(req, res);
    }

    private String resolveToken(HttpServletRequest request) {
        String authz = request.getHeader("Authorization");
        if (authz != null) {
            int idx = authz.indexOf(' ');
            if (idx > 0 && "Bearer".equalsIgnoreCase(authz.substring(0, idx).trim())) {
                return authz.substring(idx + 1).trim();
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) if ("JWT".equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
