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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "group_chat_rooms")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // ✅ JPA가 필요로 함
@AllArgsConstructor
public class GroupChatRooms {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("그룹 채팅방 PK")
	private Long id;
	
	@Column(name = "room_name", length = 255)
	@Comment("채팅방 이름")
	private String roomName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	@Comment("생성자 FK")
	private Users createdBy;
	
	@Column(name = "created_at", nullable = false)
	@Comment("생성일")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();


} 