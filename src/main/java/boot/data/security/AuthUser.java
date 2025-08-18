package boot.data.security;

import java.io.Serializable;

/**
 * SecurityContext의 Principal 로 쓰는, 얇은 인증 유저 객체
 */
public record AuthUser(Long id, String email, String role) implements Serializable {
    public boolean hasRole(String r) {
        if (r == null || role == null) return false;
        String want = r.toUpperCase();
        if (!want.startsWith("ROLE_")) want = "ROLE_" + want;
        return role.equalsIgnoreCase(want);
    }
}
