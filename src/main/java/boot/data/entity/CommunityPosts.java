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
@Table(name = "Community_posts")
public class CommunityPosts {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("게시글 PK")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("작성자 FK (users.id)")
	private Users user;
	
	@Column(nullable = false, length = 200)
	@Comment("게시글 제목")
	private String title;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	@Comment("게시글 내용")
	private String content;
	
	@Column(name = "view_count", nullable = false)
	@Comment("조회수")
	private Integer viewCount = 0;
	
	@Column(name = "created_at", nullable = false)
	@Comment("생성일")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Column(name = "updated_at", nullable = false)
	@Comment("수정일")
	private LocalDateTime updatedAt = LocalDateTime.now();
} 