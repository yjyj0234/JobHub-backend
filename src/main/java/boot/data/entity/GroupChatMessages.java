package boot.data.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Group_chat_messages")
public class GroupChatMessages {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("메시지 PK")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	@Comment("채팅방 FK")
	private GroupChatRooms room;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	@Comment("보낸 사람 FK")
	private Users sender;
	
	@Lob
	@Column(name = "message", columnDefinition = "TEXT", nullable = false)
	@Comment("메시지 내용")
	private String message;
	
	@Column(name = "sent_at", nullable = false)
	@Comment("전송 시각")
	private LocalDateTime sentAt = LocalDateTime.now();
} 