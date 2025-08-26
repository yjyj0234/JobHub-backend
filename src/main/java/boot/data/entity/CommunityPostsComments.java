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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "community_posts_comments")
@Builder
@AllArgsConstructor
@NoArgsConstructor

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
	
	@Lob
	@Column(columnDefinition = "TEXT", nullable = false)
	@Comment("댓글 내용")
	private String content;
	
	@Builder.Default
	@Column(name = "created_at", nullable = false)
	@Comment("작성일")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Builder.Default
	@Column(name = "updated_at", nullable = false)
	@Comment("수정일")
	private LocalDateTime updatedAt = LocalDateTime.now();
} 