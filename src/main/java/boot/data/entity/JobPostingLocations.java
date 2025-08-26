// 다대다 관계 중간 테이블
package boot.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.time.LocalDateTime;

/**
 * 채용공고와 지역의 다대다 관계를 관리하는 중간 테이블
 * @ManyToMany 대신 이 방식을 사용하는 이유:
 * 1. 추가 속성 관리 가능 (isPrimary 등)
 * 2. 쿼리 최적화 용이
 * 3. 관계 변경 이력 추적 가능
 */
@Data
@Entity
@Table(name = "job_posting_locations",
        indexes = {
            @Index(name = "idx_posting_id", columnList = "posting_id"),
            @Index(name = "idx_region_id", columnList = "region_id")
            })
public class JobPostingLocations {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK")
    private Long id;
    
    /**
     * 채용공고 (N:1)
     * LAZY 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    @Comment("채용공고 ID (FK)")
    private JobPostings jobPosting;
    
    /**
     * 지역 (N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @Comment("지역 ID (FK)")
    private Regions region;
    
    /**
     * 주 근무지 여부
     * 여러 지역 중 대표 지역 표시용
     */
    @Column(name = "is_primary", nullable = false)
    @Comment("주 근무지 여부")
    private boolean isPrimary = false;
    
    @Column(name = "created_at", nullable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt = LocalDateTime.now();
}