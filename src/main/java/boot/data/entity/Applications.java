package boot.data.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "applications")
public class Applications {
	
	public enum ApplicationStatus {
		APPLIED, VIEWED, INTERVIEW_REQUEST, OFFERED, HIRED, REJECTED
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("지원 고유 ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "posting_id", nullable = false)
	@Comment("공고 ID")
	private JobPostings jobPosting;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("지원자 ID")
	private Users user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resume_id", nullable = false)
	@Comment("지원에 사용된 이력서 ID")
	private Resumes resume;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Comment("지원 상태")
	private ApplicationStatus status = ApplicationStatus.APPLIED;
	
	@Column(name = "applied_at", nullable = false)
	@Comment("지원 일시")
	private Timestamp appliedAt;
	
	@Column(name = "viewed_at")
	@Comment("열람 일시")
	private Timestamp viewedAt;
} 