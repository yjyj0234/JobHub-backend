package boot.data.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

final class JsonSecurityHandlers {

    private static final ObjectMapper om = new ObjectMapper();

    private JsonSecurityHandlers() {}

    static AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> writeJson(
            response, HttpStatus.UNAUTHORIZED,
            Map.of(
                "status", 401,
                "error", "UNAUTHORIZED",
                "path", request.getRequestURI(),
                "message", messageFor(ex)
            )
        );
    }

    static AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> writeJson(
            response, HttpStatus.FORBIDDEN,
            Map.of(
                "status", 403,
                "error", "FORBIDDEN",
                "path", request.getRequestURI(),
                "message", messageFor(ex)
            )
        );
    }

    private static void writeJson(HttpServletResponse response,
                                  HttpStatus status,
                                  Map<String, Object> body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(om.writeValueAsString(body));
    }

    private static String messageFor(Exception e) {
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() : msg;
    }
}
