package boot.data.security;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security의 401/403 응답을 JSON으로 통일해서 내려주는 헬퍼.
 * - javax.*가 아니라 jakarta.*를 사용 (Spring Boot 3.x 호환)
 * - writeJson: ObjectMapper로 안전하게 직렬화
 */
public final class JsonSecurityHandlers {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSecurityHandlers() {}

    /** 인증 실패(미로그인 등) → 401 JSON */
    public static AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) -> {
            writeJson(response, request, 401, "UNAUTHORIZED", ex);
        };
    }

    /** 인가 실패(권한 없음) → 403 JSON */
    public static AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            writeJson(response, request, 403, "FORBIDDEN", ex);
        };
    }

    /** 공통 JSON 쓰기 */
    private static void writeJson(HttpServletResponse response,
                                  HttpServletRequest request,
                                  int status,
                                  String error,
                                  Exception ex) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", ex == null ? null : ex.getMessage());
        body.put("path", request.getRequestURI());
        body.put("method", request.getMethod());

        var out = response.getOutputStream();
        MAPPER.writeValue(out, body);
        out.flush();
    }
}
