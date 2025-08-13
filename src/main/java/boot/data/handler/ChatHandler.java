package boot.data.handler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import boot.data.dto.ChatMessageDto;
import boot.data.dto.ChatSendRequest;
import boot.data.service.ChatService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // roomKey -> 세션들
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var params = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        String roomKey = params.getFirst("roomKey"); // ex) u1-u2
        String userId = params.getFirst("userId");   // 숫자 문자열

        if (roomKey == null || roomKey.isBlank()) roomKey = "global";

        session.getAttributes().put("roomKey", roomKey);
        if (userId != null) session.getAttributes().put("userId", Long.valueOf(userId));

        rooms.computeIfAbsent(roomKey, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        // 1) JSON이면 그대로 파싱, 아니면 세션 정보로 보정
        ChatSendRequest req;
        try {
            req = objectMapper.readValue(payload, ChatSendRequest.class);
        } catch (Exception e) {
            // 프런트가 순수 문자열만 보낸 경우 보정
            Long uid = (Long) session.getAttributes().get("userId");
            String rk = (String) session.getAttributes().get("roomKey");
             if (uid == null || rk == null) {
            session.sendMessage(new TextMessage("{\"error\":\"missing roomKey/userId\"}"));
            return; // 연결 유지
        }
            req = new ChatSendRequest();
            req.setUserId(uid);
            req.setRoomKey(rk);
            req.setMessage(payload);
        }

        // 2) DB 저장
        ChatMessageDto saved = chatService.saveMessage(req);

        // 3) 같은 방에만 브로드캐스트
        String out = objectMapper.writeValueAsString(saved);
        Set<WebSocketSession> set = rooms.get(saved.getRoomKey());
        if (set != null) {
            for (WebSocketSession s : set) {
                if (s.isOpen()) s.sendMessage(new TextMessage(out));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomKey = (String) session.getAttributes().get("roomKey");
        if (roomKey != null) {
            Set<WebSocketSession> set = rooms.get(roomKey);
            if (set != null) set.remove(session);
        }
    }
}
