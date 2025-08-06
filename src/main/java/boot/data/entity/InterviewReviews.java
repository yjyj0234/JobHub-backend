package boot.data.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Interview_reviews")
public class InterviewReviews {
	
	public enum Difficulty {
		쉬움, 보통, 어려움
	}
	
	public enum Result {
		합격, 불합격, 보류
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("면접 후기 PK")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_posting_id")
	@Comment("공고 FK")
	private JobPostings jobPosting;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("작성자 FK")
	private Users user;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty")
	@Comment("난이도")
	private Difficulty difficulty;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "result")
	@Comment("면접 결과")
	private Result result;
	
	@Column(name = "interview_type", length = 100)
	@Comment("면접 유형")
	private String interviewType;
	
	@Column(name = "interviewers", length = 100)
	@Comment("면접 인원")
	private String interviewers;
	
	@Lob
	@Column(name = "process_text", columnDefinition = "TEXT")
	@Comment("진행 방식")
	private String processText;
	
	@Lob
	@Column(name = "question_tags", columnDefinition = "TEXT")
	@Comment("질문 유형 배열")
	private String questionTags;
	
	@Lob
	@Column(name = "tip_text", columnDefinition = "TEXT")
	@Comment("팁 및 특이사항")
	private String tipText;
	
	@Lob
	@Column(name = "question_text", columnDefinition = "TEXT")
	@Comment("질문 내용")
	private String questionText;
	
	@Lob
	@Column(name = "review_text", columnDefinition = "TEXT")
	@Comment("기타 후기")
	private String reviewText;
	
	@Column(name = "interview_date")
	@Comment("면접 날짜")
	private LocalDate interviewDate;
	
	@Column(name = "created_at", nullable = false)
	@Comment("작성일")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Column(name = "updated_at", nullable = false)
	@Comment("수정일")
	private LocalDateTime updatedAt = LocalDateTime.now();
} 