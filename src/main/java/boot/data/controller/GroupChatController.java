package boot.data.controller;


import boot.data.dto.CreateRoomReqDto;
import boot.data.dto.MessageDto;
import boot.data.dto.SendMessageReqDto;
import boot.data.dto.RoomResDto;
import boot.data.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group-chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // CORS
public class GroupChatController {

    private final GroupChatService service;

    // 방 생성
    @PostMapping("/rooms")
    public RoomResDto createRoom(@RequestBody CreateRoomReqDto req) {
        return service.createRoom(req);
    }

    // 내 방 목록
    @GetMapping("/rooms")
    public List<RoomResDto> myRooms() {
        return service.myRooms();
    }

    // 참가/나가기
    @PostMapping("/rooms/{roomId}/join")
    public void join(@PathVariable Long roomId) { service.joinRoom(roomId); }

    @DeleteMapping("/rooms/{roomId}/leave")
    public void leave(@PathVariable Long roomId) { service.leaveRoom(roomId); }

    // 메시지 히스토리
    @GetMapping("/rooms/{roomId}/messages")
    public List<MessageDto> history(@PathVariable Long roomId,
                                    @RequestParam(required = false) Long afterId) {
        return service.getMessages(roomId, afterId);
    }

    /* ===== STOMP =====
       클라가 /app/rooms/{roomId}/send 로 publish 하면 호출됨
     */
    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable Long roomId, SendMessageReqDto req) {
        // URL의 roomId를 바디에 주입해서 일관성 유지
        var fixed = new SendMessageReqDto(roomId, req.getMessage());
        service.sendMessage(fixed);
    }
}
