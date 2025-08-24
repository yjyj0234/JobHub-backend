package boot.data.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.invitechat.InviteActionDto;
import boot.data.dto.invitechat.InviteCreateDto;
import boot.data.dto.invitechat.InviteResDto;
import boot.data.service.GroupChatService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat/invites")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class ChatInviteController {
    private final GroupChatService chatService;

    @PostMapping            // COMPANY가 생성
    public InviteResDto create(@RequestBody InviteCreateDto req) {
        return chatService.inviteUser(req);
    }

    @PostMapping("/accept") // USER가 수락
    public InviteResDto accept(@RequestBody InviteActionDto req) {
        if (!req.isAccept()) throw new IllegalArgumentException("accept=true 필요");
        return chatService.acceptInvite(req.getRoomId());
    }

    @PostMapping("/decline") // USER가 거절
    public InviteResDto decline(@RequestBody InviteActionDto req) {
        if (req.isAccept()) throw new IllegalArgumentException("accept=false 필요");
        return chatService.declineInvite(req.getRoomId());
    }

    @GetMapping("/me/pending") // USER가 받은 대기중 초대 목록
    public List<InviteResDto> myPending() {
        return chatService.myPendingInvites();
    }
}
