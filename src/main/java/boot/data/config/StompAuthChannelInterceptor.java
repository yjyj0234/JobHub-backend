package boot.data.config;

import boot.data.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        StompCommand cmd = acc.getCommand();
        if (cmd == null) return message;

        // 1) CONNECT: Authorization 헤더에서 토큰 추출 → 검증 → 사용자 주입
        if (StompCommand.CONNECT.equals(cmd)) {
            String header = firstNonNull(
                acc.getFirstNativeHeader("Authorization"),
                acc.getFirstNativeHeader("authorization")
            );
            if (header != null && !header.isBlank()) {
                String token = header.startsWith("Bearer ") ? header.substring(7) : header;
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        Authentication uidAuth = jwtTokenProvider.getAuthentication(token);
                        // STOMP 세션 Principal
                        acc.setUser(uidAuth);

                        // 현재 메시지 처리 스레드의 SecurityContext도 채움
                        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                        ctx.setAuthentication(uidAuth);
                        SecurityContextHolder.setContext(ctx);

                        log.debug("[STOMP] CONNECT auth uid={} authorities={}",
                                uidAuth.getName(), uidAuth.getAuthorities());
                    } else {
                        log.warn("[STOMP] invalid token");
                    }
                } catch (Exception e) {
                    log.warn("[STOMP] token error: {}", e.getMessage());
                }
            }
        }

        // 2) 이후 SEND/SUBSCRIBE 프레임에서도 Principal→SecurityContext 동기화
        if (acc.getUser() instanceof Authentication authn) {
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(authn);
            SecurityContextHolder.setContext(ctx);
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        // 스레드풀 재사용 대비: 컨텍스트 누수 방지
        SecurityContextHolder.clearContext();
    }

    private static String firstNonNull(String a, String b) {
        return (a != null) ? a : b;
    }
}