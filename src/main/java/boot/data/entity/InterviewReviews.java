package boot.data.entity;

import java.time.LocalDate;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "interview_reviews") // ← 실제 테이블명
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewReviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("리뷰 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("작성자 FK (users.id)")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    @Comment("채용공고 FK (job_postings.id)")
    private JobPostings jobPosting;

    @Column(name = "interview_type", length = 100)
    @Comment("인터뷰 유형(대면/화상/전화 등)")
    private String interviewType;

    @Column(name = "interviewers", length = 100)
    @Comment("면접관 정보(간단히)")
    private String interviewers;

    @Column(name = "difficulty", columnDefinition = "ENUM('쉬움','보통','어려움')")
    @Comment("난이도")
    private String difficulty;

    @Lob
    @Column(name = "process_text", columnDefinition = "TEXT")
    @Comment("면접 진행 과정")
    private String processText;

    @Lob
    @Column(name = "question_tags", columnDefinition = "TEXT")
    @Comment("질문 태그(쉼표/JSON 등)")
    private String questionTags;

    @Lob
    @Column(name = "question_text", columnDefinition = "TEXT")
    @Comment("질문 상세")
    private String questionText;

    @Column(name = "result", columnDefinition = "ENUM('불합격','합격')")
    @Comment("결과")
    private String result;
    

    @Lob
    @Column(name = "review_text", columnDefinition = "TEXT")
    @Comment("총평/후기")
    private String reviewText;

    @Lob
    @Column(name = "tip_text", columnDefinition = "TEXT")
    @Comment("팁/조언")
    private String tipText;

    @Column(name = "interview_date")
    @Comment("면접 일자")
    private LocalDate interviewDate;

    @Column(name = "created_at", nullable = false)
    @Comment("생성일")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("수정일")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
