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
@Table(name = "one_to_one_chat_rooms")
public class OneToOneChatRooms {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("메세지 PK")
	private Long id;
	
	@Column(name = "room_key", nullable = false, length = 100)
	@Comment("채팅방 식별자")
	private String roomKey;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("유저(users.id)")
	private Users user;
	
	@Column(name = "created_at", nullable = false)
	@Comment("생성일")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Lob
	@Column(name = "message", columnDefinition = "TEXT", nullable = false)
	@Comment("채팅 메시지")
	private String message;
	
	@Column(name = "sent_at", nullable = false)
	@Comment("전송 시각")
	private LocalDateTime sentAt = LocalDateTime.now();
} 