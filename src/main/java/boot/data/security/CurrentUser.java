package boot.data.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 서비스 레이어에서 현재 로그인 사용자에 접근하기 위한 헬퍼
 */
@Component
public class CurrentUser {

    public Optional<AuthUser> get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();
        Object p = auth.getPrincipal();
        if (p instanceof AuthUser au) return Optional.of(au);
        return Optional.empty();
    }

    public Long idOrThrow() {
        return get().map(AuthUser::id)
            .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다."));
    }

    public String emailOrNull() {
        return get().map(AuthUser::email).orElse(null);
    }

    public String roleOrNull() {
        return get().map(AuthUser::role).orElse(null);
    }
}
