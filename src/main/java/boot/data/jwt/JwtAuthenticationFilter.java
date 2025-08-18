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

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
    
        String token = resolveToken(request);
    
        if (token == null) {
            // 디버깅용
            System.out.println("[JWT] no token found on " + request.getRequestURI());
        } else if (!jwtTokenProvider.validateToken(token)) {
            System.out.println("[JWT] invalid token on " + request.getRequestURI());
        } else if (SecurityContextHolder.getContext().getAuthentication() == null) { // ✅ 덮어쓰기 방지
            Authentication auth = jwtTokenProvider.getAuthentication(token); // ✅ principal=AuthUser 여야 함
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("[JWT] authentication set for uid=" +
                ((boot.data.security.AuthUser)auth.getPrincipal()).id());
        }
    
        chain.doFilter(request, response);
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
}