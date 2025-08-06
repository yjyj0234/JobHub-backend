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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Group_chat_members")
public class GroupChatMembers {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("참여자 PK")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	@Comment("그룹 채팅방 FK")
	private GroupChatRooms room;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("참여자 FK")
	private Users user;
	
	@Column(name = "joined_at", nullable = false)
	@Comment("입장 시각")
	private LocalDateTime joinedAt = LocalDateTime.now();
} 