//인기 검색어 관리 entity
package boot.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import java.time.LocalDateTime;

/**
 * 검색 키워드 통계 관리
 * 인기 검색어, 추천 검색어 기능용
 */
@Data
@Entity
@Table(name = "search_keywords",
       indexes = {
           @Index(name = "idx_keyword", columnList = "keyword"),
           @Index(name = "idx_search_count", columnList = "search_count DESC")
       })
public class SearchKeywords {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100, unique = true)
    @Comment("검색 키워드")
    private String keyword;
    
    @Column(name = "search_count", nullable = false)
    @Comment("검색 횟수")
    private Long searchCount = 0L;
    
    @Column(name = "last_searched_at")
    @Comment("마지막 검색 시간")
    private LocalDateTime lastSearchedAt;
    
    @Column(name = "is_trending", nullable = false)
    @Comment("급상승 키워드 여부")
    private boolean isTrending = false;
    
    /**
     * 검색 횟수 증가 메서드
     */
    public void incrementSearchCount() {
        this.searchCount++;
        this.lastSearchedAt = LocalDateTime.now();
    }
}