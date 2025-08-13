package boot.data.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
    @PostMapping("/message")
    public ResponseEntity<ChatMessageDto> saveMessage(@Validated @RequestBody ChatSendRequest req) { //@Validated 스프링의 검증 트리거 @RequestBody에 붙이면 DTO의 @NotNull, @NotBlank 같은 Bean Validation 제약을 검사
        ChatMessageDto saved = chatService.saveMessage(req);
        return ResponseEntity.ok(saved);
    }
}
