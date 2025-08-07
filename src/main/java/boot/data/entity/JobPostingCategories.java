//다대다 관계 중간 테이블
package boot.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.time.LocalDateTime;

/**
 * 채용공고와 직무 카테고리의 다대다 관계 관리
 * 하나의 공고가 여러 직무를 모집할 수 있음
 * 예: "풀스택 개발자" 공고 -> 백엔드, 프론트엔드 카테고리 동시 매핑
 */
@Data
@Entity
@Table(name = "job_posting_categories",
        indexes = {
        @Index(name = "idx_posting_category", columnList = "posting_id, category_id"),
        @Index(name = "idx_category_id", columnList = "category_id")
        })
public class JobPostingCategories {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    @Comment("채용공고 ID (FK)")
    private JobPostings jobPosting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @Comment("직무 카테고리 ID (FK)")
    private JobCategories jobCategory;
    
    /**
     * 주 직무 여부
     * 여러 직무 중 메인 직무 표시
     */
    @Column(name = "is_primary", nullable = false)
    @Comment("주 직무 여부")
    private boolean isPrimary = false;
    
    @Column(name = "created_at", nullable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt = LocalDateTime.now();
}