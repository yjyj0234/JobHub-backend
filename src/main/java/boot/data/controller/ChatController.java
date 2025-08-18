package boot.data.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.ChatMessageDto;
import boot.data.dto.ChatSendRequest;
import boot.data.service.ChatService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 메시지 저장 (웹소켓과 별개로 REST로도 저장 가능)
    @MessageMapping("/message")
    public ResponseEntity<ChatMessageDto> saveMessage(@Validated @RequestBody ChatSendRequest req) {
        ChatMessageDto saved = chatService.saveMessage(req);
        return ResponseEntity.ok(saved);
    }
}
