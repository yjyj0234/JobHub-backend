package boot.data.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import boot.data.dto.*;
import boot.data.dto.invitechat.InviteCreateDto;
import boot.data.dto.invitechat.InviteResDto;
import boot.data.entity.*;
import boot.data.repository.*;

import boot.data.security.CurrentUser;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChatService {

    private final GroupChatRoomsRepository roomsRepo;
    private final GroupChatMembersRepository membersRepo;
    private final GroupChatMessagesRepository messagesRepo;
    private final UsersRepository usersRepo;                // ✅ Users 조회용
    private final UserProfilesRepository userProfilesRepo; // senderName 조회용
    private final CurrentUser currentUser;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Pattern INVITE_PATTERN = Pattern.compile("^INVITE:\\d+:\\d+$");
    private static final Pattern DM_PATTERN     = Pattern.compile("^DM:\\d+:\\d+$");

    /* 방 생성 */
    @Transactional
    public RoomResDto createRoom(CreateRoomReqDto req) {
        Long uid = currentUser.idOrThrow();
        Users me = usersRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + uid));

        GroupChatRooms room = GroupChatRooms.builder()
                .roomName(req.getRoomName())
                .createdBy(me) // ✅ Users 객체로 세팅
                .build();
        roomsRepo.save(room);

        GroupChatMembers member = GroupChatMembers.builder()
                .room(room)
                .user(me) // ✅ Users 객체로 세팅
                .build();
        membersRepo.save(member);

        
        return toRoomRes(room);
    }

    /* 방 참가 */
    @Transactional
public Long joinRoom(Long roomId) {
    Long uid = currentUser.idOrThrow();
    Users me = usersRepo.findById(uid)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + uid));

    GroupChatRooms room = roomsRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

    boolean isNew = false;
    if (!membersRepo.existsByRoom_IdAndUser_Id(roomId, uid)) {
        GroupChatMembers m = GroupChatMembers.builder()
                .room(room)
                .user(me)
                .build();
        membersRepo.save(m);
        isNew = true;
    }

    // ✅ 처음 들어온 사람에게만 '입장했습니다' 브로드캐스트
    if (isNew) {
        String joinerName = userProfilesRepo.findByUserId(uid)
                .map(UserProfiles::getName)
                .orElse("알 수 없음");

                

        MessageDto dto = new MessageDto();
        dto.setId(null);
        dto.setSenderId(uid);
        dto.setSenderName(joinerName);
        dto.setMessage(joinerName + " 님이 입장했습니다.");
        dto.setSentAt(LocalDateTime.now());
        dto.setSystem(true);
        dto.setType("JOIN");

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, dto);
    }

    return uid;
}

    // 방 멤버 목록 조회
    public List<MemberDto> getRoomMembers(Long roomId) {
   
    

    // 멤버 목록 조회
    List<GroupChatMembers> members = membersRepo.findByRoom_Id(roomId);

    return members.stream()
            .map(m -> new MemberDto(
                    m.getUser().getId(),
                    userProfilesRepo.findByUserId(m.getUser().getId())
                            .map(UserProfiles::getName)
                            .orElse("알 수 없음")
            ))
            .toList();
}

    /* 방 나가기 */
    @Transactional
public void leaveRoom(Long roomId) {
    Long uid = currentUser.idOrThrow();



    // 나가기 이전에 이름 확보
    String leaverName = userProfilesRepo.findByUserId(uid)
            .map(UserProfiles::getName)
            .orElse("알 수 없음");

    // 멤버 삭제
    membersRepo.deleteByRoom_IdAndUserId(roomId, uid); // 주의: 메서드 시그니처 확인
    


    
    // ✅ 저장 없이 즉시 브로드캐스트
    MessageDto dto = new MessageDto();
    dto.setId(null);
    dto.setSenderId(uid);
    dto.setSenderName(leaverName);
    dto.setMessage(leaverName + " 님이 방을 나갔습니다.");
    dto.setSentAt(java.time.LocalDateTime.now());
    dto.setSystem(true);
    dto.setType("LEAVE");

    messagingTemplate.convertAndSend("/topic/rooms/" + roomId, dto);
}

    /* 내 방 목록 */
    public List<RoomResDto> myRooms() {
        Long uid = currentUser.idOrThrow();
        return roomsRepo.findMyRooms(uid).stream()
                .filter(r -> !isInterviewRoom(r)) // 면접 전용 방 제외
                .map(this::toRoomRes)
                .toList();
    }

    //방이름가져오기
    public RoomResDto getRoom(Long roomId) {
        GroupChatRooms room = roomsRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));
        return toRoomRes(room);
    }

    /* 메시지 히스토리 */
    public List<MessageDto> getMessages(Long roomId, Long afterId) {
        Long uid = tryCurrentUserId();

        List<GroupChatMessages> raw = (afterId == null)
                ? messagesRepo.findTop50ByRoom_IdOrderByIdDesc(roomId)
                : messagesRepo.findByRoom_IdAndIdGreaterThanOrderByIdAsc(roomId, afterId);

        if (afterId == null) {
            raw = raw.stream().sorted(Comparator.comparingLong(GroupChatMessages::getId)).toList();
        }
        Long currentUid = uid;
        return raw.stream().map(m -> toMessageDto(m, currentUid)).toList();
    }

    /* 메시지 전송 */
@Transactional
public MessageDto sendMessageFromPrincipal(Long roomId, String message, Principal principal) {
    Long uid = resolveUidFromPrincipal(principal);
    return sendMessage(roomId, message, uid);
}

/** 다양한 Principal 타입/표현을 견고하게 처리 */
private Long resolveUidFromPrincipal(Principal principal) {
    if (principal == null) throw new IllegalStateException("인증 필요");

    // 1) Authentication인 경우 principal 객체에서 꺼내보기
    if (principal instanceof Authentication auth) {
        Object p = auth.getPrincipal();
        // (1) 커스텀 AuthUser에 id가 있는 경우
        if (p != null && p.getClass().getSimpleName().equals("AuthUser")) {
            try {
                // 리플렉션으로 getId() 시도 (AuthUser 타입 의존 제거)
                var m = p.getClass().getMethod("getId");
                Object idObj = m.invoke(p);
                if (idObj != null) return Long.parseLong(idObj.toString());
            } catch (Exception ignore) {}
        }
        // (2) UserDetails 사용자명에 uid를 담는 경우
        if (p instanceof UserDetails ud) {
            try { return Long.parseLong(ud.getUsername()); } catch (Exception ignore) {}
        }
    }

    // 2) Principal.getName()이 uid인 경우
    try { return Long.parseLong(principal.getName()); } catch (Exception ignore) {}

    // 3) "id=123" 같은 문자열에서 추출 (예: "AuthUser[id=19, ...]")
    var m = java.util.regex.Pattern.compile("\\bid=(\\d+)\\b").matcher(principal.getName());
    if (m.find()) return Long.parseLong(m.group(1));

    throw new IllegalStateException("STOMP Principal에서 uid 추출 실패: " + principal.getName());
}

// 메시지 전송
@Transactional
public MessageDto sendMessage(Long roomId, String message, Long senderId) {
    Users me = usersRepo.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + senderId));

    GroupChatRooms room = roomsRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

    if (!membersRepo.existsByRoom_IdAndUser_Id(room.getId(), senderId)) {
        throw new IllegalStateException("not a member of room: " + room.getId());
    }

    GroupChatMessages saved = GroupChatMessages.builder()
            .room(room)
            .sender(me)
            .message(message)
            .build();
    messagesRepo.save(saved);

    MessageDto dto = toMessageDto(saved, null); // 현재 사용자 ID는 null로 설정 (STOMP에서 Principal 사용)
    messagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), dto);
    return dto;
}

//방 탐색 (전체 공개 방)
public List<RoomResDto> exploreRooms() {
    // 전체 방 최신순
    return roomsRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .stream()
            .filter(r -> !isInterviewRoom(r)) // 면접 전용 방 제외
            .map(this::toRoomRes)
            .toList();
}

// 방 생성자만 방 삭제 가능
@Transactional
public void deleteRoom(Long roomId, Principal principal) {
    Long uid = currentUser.idOrThrow();
    GroupChatRooms room = roomsRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));
    if (!room.getCreatedBy().getId().equals(uid)) {
        throw new IllegalStateException("not room creator: " + roomId);
    }
        messagesRepo.deleteByRoom_Id(roomId);
        membersRepo.deleteByRoom_Id(roomId);
        roomsRepo.delete(room);
    messagingTemplate.convertAndSend("/topic/rooms", exploreRooms());
}

// 1대1 면접제안 채팅방

// 초대 생성
@Transactional
public InviteResDto inviteUser(InviteCreateDto req) {
    if (!"COMPANY".equals(currentUser.roleOrNull())) {
        throw new IllegalStateException("면접 초대는 COMPANY만 가능합니다.");
    }

    Long inviterId = currentUser.idOrThrow();
    Users inviter = usersRepo.findById(inviterId)
            .orElseThrow(() -> new IllegalArgumentException("초대한 사용자 없음: " + inviterId));

    Users invitee = usersRepo.findById(req.getTargetUserId())
            .orElseThrow(() -> new IllegalArgumentException("초대 대상 없음: " + req.getTargetUserId()));

    // 대상은 USER만
    if (invitee.getUserType() == null || invitee.getUserType() != boot.data.type.UserType.USER) {
        throw new IllegalArgumentException("대상은 USER 유형만 가능합니다.");
    }

    // 이미 수락된 1:1 방이 있으면 중복 방지
    if (roomsRepo.findOneToOne(inviterId, invitee.getId()).isPresent()) {
        throw new IllegalStateException("이미 1:1 채팅방이 존재합니다.");
    }

    // 이미 대기중 초대가 있으면 중복 방지
    if (roomsRepo.findPendingInvite(inviterId, invitee.getId()).isPresent()) {
        throw new IllegalStateException("이미 대기 중인 초대가 있습니다.");
    }

    // 방 생성 (회사만 멤버)
    GroupChatRooms room = GroupChatRooms.builder()
            .roomName("INVITE:" + inviterId + ":" + invitee.getId())
            .createdBy(inviter)
            .build();
    roomsRepo.save(room);

    membersRepo.save(GroupChatMembers.builder()
            .room(room)
            .user(inviter)     // 회사만 먼저 입장
            .build());

    // 선택: 초대 메시지(시스템 느낌)
    String msg = (req.getMessage() == null || req.getMessage().isBlank())
            ? "면접 초대가 도착했습니다."
            : "[면접제안] " + req.getMessage();

    messagesRepo.save(GroupChatMessages.builder()
            .room(room)
            .sender(inviter)
            .message(msg)
            .build());

    // 초대 알림(구독 채널 예시)
    try {
        MessageDto notice = new MessageDto();
        notice.setSystem(true);
        notice.setType("INVITE");
        notice.setRoomId(room.getId());
        notice.setSenderId(inviterId);
        notice.setMessage("면접 초대가 도착했습니다.");
        notice.setSentAt(java.time.LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/invites/" + invitee.getId(), notice);
    } catch (Exception ignore) {}

    return InviteResDto.builder()
            .roomId(room.getId())
            .inviterId(inviterId)
            .inviteeId(invitee.getId())
            .status("PENDING")
            .createdAt(room.getCreatedAt())
            .respondedAt(null)
            .build();
}

//초대 수락
@Transactional
public InviteResDto acceptInvite(Long roomId) {
    Long meId = currentUser.idOrThrow();

    GroupChatRooms room = roomsRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

    long[] pair = parseInviteRoomName(room.getRoomName()); // [inviterId, inviteeId]
    Long inviterId = pair[0];
    Long inviteeId = pair[1];

    if (!inviteeId.equals(meId)) {
        throw new IllegalStateException("본인에게 온 초대가 아닙니다.");
    }
    if (room.getCreatedBy().getUserType() != boot.data.type.UserType.COMPANY) {
        throw new IllegalStateException("유효하지 않은 초대 방입니다.");
    }
    if (membersRepo.existsByRoom_IdAndUser_Id(roomId, meId)) {
        // 이미 수락됨
        return InviteResDto.builder()
                .roomId(room.getId())
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .status("ACCEPTED")
                .createdAt(room.getCreatedAt())
                .respondedAt(java.time.LocalDateTime.now())
                .build();
    }
    // 대기중: 멤버 1명(회사)만 있어야 정상
    if (membersRepo.countByRoom_Id(roomId) != 1) {
        throw new IllegalStateException("초대 대기 상태가 아닙니다.");
    }

    // 멤버 추가 (USER)
    membersRepo.save(GroupChatMembers.builder()
            .room(room)
            .user(usersRepo.getReferenceById(meId))
            .build());

    // 방 이름 변경(선택)
    long a = Math.min(inviterId, inviteeId);
    long b = Math.max(inviterId, inviteeId);
    room.setRoomName("DM:" + a + ":" + b);
    roomsRepo.save(room);

    // 시스템 메시지
    MessageDto dto = new MessageDto();
    dto.setSystem(true);
    dto.setType("INVITE_ACCEPTED");
    dto.setRoomId(room.getId());
    dto.setSenderId(meId);
    dto.setMessage("초대를 수락했습니다. 대화를 시작할 수 있어요.");
    dto.setSentAt(java.time.LocalDateTime.now());
    messagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), dto);

    return InviteResDto.builder()
            .roomId(room.getId())
            .inviterId(inviterId)
            .inviteeId(inviteeId)
            .status("ACCEPTED")
            .createdAt(room.getCreatedAt())
            .respondedAt(java.time.LocalDateTime.now())
            .build();
}

//초대 거절
@Transactional
public InviteResDto declineInvite(Long roomId) {
    Long meId = currentUser.idOrThrow();

    GroupChatRooms room = roomsRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

    long[] pair = parseInviteRoomName(room.getRoomName());
    Long inviterId = pair[0];
    Long inviteeId = pair[1];

    if (!inviteeId.equals(meId)) {
        throw new IllegalStateException("본인에게 온 초대가 아닙니다.");
    }
    if (room.getCreatedBy().getUserType() != boot.data.type.UserType.COMPANY) {
        throw new IllegalStateException("유효하지 않은 초대 방입니다.");
    }
    // 아직 수락 전이어야 함
    if (membersRepo.countByRoom_Id(roomId) != 1) {
        throw new IllegalStateException("초대 대기 상태가 아닙니다.");
    }

    // 방/메시지 정리
    messagesRepo.deleteByRoom_Id(roomId);
    membersRepo.deleteByRoom_Id(roomId);
    roomsRepo.delete(room);

    // (선택) 초대한 회사에게 거절 알림
    try {
        MessageDto notice = new MessageDto();
        notice.setSystem(true);
        notice.setType("INVITE_DECLINED");
        notice.setRoomId(roomId);
        notice.setSenderId(meId);
        notice.setMessage("초대가 거절되었습니다.");
        notice.setSentAt(java.time.LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/invites/" + inviterId, notice);
    } catch (Exception ignore) {}

    return InviteResDto.builder()
            .roomId(roomId)
            .inviterId(inviterId)
            .inviteeId(inviteeId)
            .status("DECLINED")
            .createdAt(room.getCreatedAt())
            .respondedAt(java.time.LocalDateTime.now())
            .build();
}

//내가 받은 대기중 초대 목록
public List<InviteResDto> myPendingInvites() {
    Long meId = currentUser.idOrThrow();
    return roomsRepo.findPendingInvitesForUser(meId).stream()
            .map(r -> {
                long[] p = parseInviteRoomName(r.getRoomName());
                return InviteResDto.builder()
                        .roomId(r.getId())
                        .inviterId(p[0])
                        .inviteeId(p[1])
                        .status("PENDING")
                        .createdAt(r.getCreatedAt())
                        .respondedAt(null)
                        .build();
            })
            .toList();
}


/** 회사-개인 1:1 면접 전용 방 목록 (내가 속한 것만) */
@Transactional(readOnly = true)
public List<RoomResDto> myInterviewRooms() {
    Long uid = currentUser.idOrThrow();

    // 내가 멤버인 방들 → 면접 전용 규칙으로 필터
    var interviewRooms = roomsRepo.findMyRooms(uid).stream()
            .filter(this::isInterviewRoom) // 아래 헬퍼로 판별
            .map(this::toRoomRes)
            // 최신 활동순(마지막 메시지 시각 없으면 생성 시각)
            .sorted((a, b) -> {
                var at = a.getLastSentAt() != null ? a.getLastSentAt() : a.getCreatedAt();
                var bt = b.getLastSentAt() != null ? b.getLastSentAt() : b.getCreatedAt();
                return bt.compareTo(at);
            })
            .toList();

    return interviewRooms;
}

/** 면접 전용 방 판별:
 *  - INVITE: createdBy 가 COMPANY
 *  - DM: 멤버에 COMPANY 1명 + USER 1명
 */
private boolean isInterviewRoom(GroupChatRooms room) {
    String name = room.getRoomName();
    if (name == null) return false;

     name = name.strip(); // 앞뒤 공백 방어 (Java 11+)

    // INVITE:<inviterId>:<inviteeId>  또는  DM:<a>:<b> 형식이면 면접방으로 간주
    return INVITE_PATTERN.matcher(name).matches()
        || DM_PATTERN.matcher(name).matches();

    // // 수락된 1:1 DM 방: DM:<a>:<b>
    // if (name.startsWith("DM:")) {
    //     // 멤버 조회해서 COMPANY/USER 한 명씩 있는지 확인
    //     var members = membersRepo.findByRoom_Id(room.getId());
    //     boolean hasCompany = members.stream()
    //             .anyMatch(m -> m.getUser() != null
    //                     && m.getUser().getUserType() == boot.data.type.UserType.COMPANY);
    //     boolean hasUser = members.stream()
    //             .anyMatch(m -> m.getUser() != null
    //                     && m.getUser().getUserType() == boot.data.type.UserType.USER);
    //     return hasCompany && hasUser;
    // }

    // return false;
}

//1대1 채팅방 헬퍼
private long[] parseInviteRoomName(String roomName) {
    if (roomName == null || !roomName.startsWith("INVITE:"))
        throw new IllegalStateException("초대 방이 아닙니다.");
    String[] parts = roomName.split(":");
    if (parts.length != 3)
        throw new IllegalStateException("잘못된 초대 방 이름 형식입니다.");
    try {
        long inviterId = Long.parseLong(parts[1]);
        long inviteeId = Long.parseLong(parts[2]);
        return new long[]{inviterId, inviteeId};
    } catch (NumberFormatException e) {
        throw new IllegalStateException("초대 방 파싱 실패: " + roomName);
    }
}


    /* ===== Helper ===== */

    private RoomResDto toRoomRes(GroupChatRooms room) {
        boolean isOwner = currentUser.idOrThrow().equals(room.getCreatedBy().getId());
        int memberCnt = membersRepo.countByRoom_Id(room.getId()); // 방 멤버 수
        var lastOpt = messagesRepo.findTop1ByRoom_IdOrderByIdDesc(room.getId()); // 마지막 메시지 조회

        return RoomResDto.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .createdBy(room.getCreatedBy().getId()) // Users → id 추출
                .createdAt(room.getCreatedAt())
                .memberCount(memberCnt)
                .isOwner(isOwner)
                .lastMessage(lastOpt.map(GroupChatMessages::getMessage).orElse(null))
                .lastSentAt(lastOpt.map(GroupChatMessages::getSentAt).orElse(null))
                .build();
    }

    private MessageDto toMessageDto(GroupChatMessages m, Long currentUid) {
        String senderName = userProfilesRepo.findByUserId(m.getSender().getId())
                .map(p -> p.getName()).orElse("알 수 없음");
        boolean mine = (currentUid != null && currentUid.equals(m.getSender().getId()));

        return MessageDto.builder()
                .id(m.getId())
                .roomId(m.getRoom().getId())
                .senderId(m.getSender().getId()) // ✅ Users → id 추출
                .senderName(senderName)
                .sentAt(m.getSentAt())
                .message(m.getMessage())
                .mine(mine)
                .build();
    }

    // 현재 사용자 ID를 안전하게 가져옵니다.
    private Long tryCurrentUserId() {
        try { return currentUser.idOrThrow(); } catch (Exception e) { return null; }
    }

   
    

    
}