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
@Table(name = "Community_posts_comments")
public class CommunityPostsComments {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("댓글 PK")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	@Comment("게시글 FK")
	private CommunityPosts post;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("작성자 FK")
	private Users user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@Comment("부모 댓글 ID")
	private CommunityPostsComments parent;
	
	@Lob
	@Column(columnDefinition = "TEXT", nullable = false)
	@Comment("댓글 내용")
	private String content;
	
	@Column(name = "is_deleted", nullable = false)
	@Comment("삭제 여부")
	private boolean isDeleted = false;
	
	@Column(name = "created_at", nullable = false)
	@Comment("작성일")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Column(name = "updated_at", nullable = false)
	@Comment("수정일")
	private LocalDateTime updatedAt = LocalDateTime.now();
} 