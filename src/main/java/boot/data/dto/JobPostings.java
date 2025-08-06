package boot.data.dto;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "job_postings")
public class JobPostings {
	
	//공고상태를 나타내는 Enum
	public enum PostingStatus {
        DRAFT, OPEN, CLOSED, EXPIRED

	}
	// 마감 유형을 나타내는 Enum
    public enum CloseType {
        DEADLINE, UNTIL_FILLED, CONTINUOUS, PERIODIC
    }
    
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
    private LocalDateTime createAt=LocalDateTime.now();
    
    @Column(name = "updated_at",nullable = false)
    @Comment("업데이트 일시")
    private LocalDateTime updatedAt=LocalDateTime.now();
}
