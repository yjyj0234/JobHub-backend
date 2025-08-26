package boot.data.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Comment;

import boot.data.type.CloseType;
import boot.data.type.PostingStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "job_postings")
public class JobPostings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable = false)
    private Companies company; // company_id 컬럼 대신함
    
    @Column(nullable = false,length = 255)
    private String title;
    
    //enum 타입을 어떻게 db에 저장할지(지금은 STRING)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("공고 상태")
    private PostingStatus status=PostingStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "close_type",nullable = false)
    private CloseType closeType;
    
    @Column(name = "is_remote",nullable = false)
    @Comment("재택근무 가능 여부")
    private boolean isRemote = false;
    
    @Column(name = "view_count",nullable = false)
    private Integer viewCount=0;
    
    @Column(name = "application_count",nullable = false)
    @Comment("지원자수")
    private Integer applicationCount=0;
    
    @Column(name = "open_date")
    private LocalDateTime openDate;
    
    @Column(name = "close_date")
    private LocalDateTime closeDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by",nullable = false)
    @Comment("작성자 ID")
    private Users createdBy;
    
    @Column(name = "created_at", nullable =  false, updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
    
    @Column(name = "updated_at",nullable = false)
    @Comment("업데이트 일시")
    private LocalDateTime updatedAt=LocalDateTime.now();

    
    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    private String description;

    /**
     * 채용공고와 지역의 다대다 관계
     * 하나의 공고가 여러 지역을 가질 수 있음 (예: 서울, 부산 동시 채용)
     * @ManyToMany 대신 중간 테이블 엔티티를 만들어 관리 (쿼리 최적화)
     */
    @OneToMany(mappedBy = "jobPosting", fetch = FetchType.LAZY)
    @Comment("채용공고 지역 매핑")
    private List<JobPostingLocations> jobPostingLocations = new ArrayList<>();
    
    /**
     * 채용공고와 직무 카테고리의 다대다 관계
     * 하나의 공고가 여러 직무를 포함할 수 있음 (예: 백엔드+프론트엔드)
     */
    @OneToMany(mappedBy = "jobPosting", fetch = FetchType.LAZY)
    @Comment("채용공고 직무 카테고리 매핑")
    private List<JobPostingCategories> jobPostingCategories = new ArrayList<>();
    
    /**
     * 검색 최적화를 위한 통합 검색 필드
     * 제목, 회사명, 직무명을 합쳐서 저장 (전문 검색용)
     */
    @Column(name = "search_text", columnDefinition = "TEXT")
    @Comment("통합 검색을 위한 텍스트 (제목+회사명+직무명)")
    private String searchText;
    
    /**
     * 조회수 증가 메서드 (동시성 고려)
     */
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    /**
     * 채용공고 조건 (1:1 관계)
     */
    @OneToOne(mappedBy = "jobPosting", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private JobPostingConditions jobPostingConditions;

}
