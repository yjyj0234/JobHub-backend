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
@Table(name = "One_to_one_chat_rooms")
public class OneToOneChatRooms {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("1:1 채팅방 PK")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("유저(users.id)")
	private Users user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "companies_id", nullable = false)
	@Comment("기업 (companies.id)")
	private Companies company;
	
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