package boot.data.service;

import java.util.Comparator;
import java.util.List;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void joinRoom(Long roomId) {
        Long uid = currentUser.idOrThrow();
        Users me = usersRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + uid));

        GroupChatRooms room = roomsRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

        if (!membersRepo.existsByRoom_IdAndUser_Id(roomId, uid)) {
            GroupChatMembers m = GroupChatMembers.builder()
                    .room(room)
                    .user(me) // ✅ Users 객체로 세팅
                    .build();
            membersRepo.save(m);
        }
    }

    /* 방 나가기 */
    @Transactional
    public void leaveRoom(Long roomId) {
        Long uid = currentUser.idOrThrow();
        membersRepo.deleteByRoom_IdAndUserId(roomId, uid);
    }

    /* 내 방 목록 */
    public List<RoomResDto> myRooms() {
        Long uid = currentUser.idOrThrow();
        return roomsRepo.findMyRooms(uid).stream()
                .map(this::toRoomRes)
                .toList();
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
    public MessageDto sendMessage(SendMessageReqDto req) {
        Long uid = currentUser.idOrThrow();
        Users me = usersRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + uid));

        GroupChatRooms room = roomsRepo.findById(req.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + req.getRoomId()));

        if (!membersRepo.existsByRoom_IdAndUser_Id(room.getId(), uid)) {
            throw new IllegalStateException("not a member of room: " + room.getId());
        }

        GroupChatMessages saved = GroupChatMessages.builder()
                .room(room)
                .sender(me) // ✅ Users 객체로 세팅
                .message(req.getMessage())
                .build();
        messagesRepo.save(saved);

        MessageDto dto = toMessageDto(saved, null);



        messagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), dto);
        return dto;
    }

    /* ===== Helper ===== */

    private RoomResDto toRoomRes(GroupChatRooms room) {
        int memberCnt = membersRepo.countByRoom_Id(room.getId());
        var lastOpt = messagesRepo.findTop1ByRoom_IdOrderByIdDesc(room.getId());

        return RoomResDto.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .createdBy(room.getCreatedBy().getId()) // ✅ Users → id 추출
                .createdAt(room.getCreatedAt())
                .memberCount(memberCnt)
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

    private Long tryCurrentUserId() {
        try { return currentUser.idOrThrow(); } catch (Exception e) { return null; }
    }
}