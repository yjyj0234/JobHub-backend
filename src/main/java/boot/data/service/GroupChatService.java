package boot.data.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import boot.data.dto.*;
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

    /* 방 나가기 */
    @Transactional
public void leaveRoom(Long roomId) {
    Long uid = currentUser.idOrThrow();

    GroupChatRooms room = roomsRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

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

public List<RoomResDto> exploreRooms() {
    // 전체 방 최신순
    return roomsRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .stream()
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