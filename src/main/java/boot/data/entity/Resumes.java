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
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Resumes {
	
	//자바에선 long 타입이 mysql BIGINT에 대응
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("이력서 고유 ID")
	private Long id;

	    // 'user_id' 컬럼과 매핑되는 관계 설정
	 	//FetchType.EAGER (즉시 로딩) : 메인 엔티티 조회 시 즉시 로딩(성능 저하)
	 	//FetchType.LAZY (지연 로딩) : 연관된 객체 사용 시점에 로딩(성능 최적화에 유리)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@Comment("사용자 ID")
	private Users user;

	@Column(nullable = false)
	@Comment("이력서 제목")
	private String title;

	@Column(name = "is_primary", nullable = false)
	@Comment("대표 이력서 여부")
	private boolean isPrimary = false;

	@Column(name = "is_public", nullable = false)
	@Comment("공개 여부")
	private boolean isPublic = false;

	@Column(name = "completion_rate", nullable = false)
	@Comment("완성도 (%)")
	private short completionRate = 0;

	@Column(name = "status", nullable = false, length = 20)
    @Comment("이력서 상태 (작성 중, 작성 완료)")
    private String status = "작성 중";
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Comment("수정일시")
    private LocalDateTime updatedAt = LocalDateTime.now();
	

	 // ========== 편의 메서드들 ==========
    
    //수정 시간 자동업데이트
    public void updateModifiedTime() {
        this.updatedAt = LocalDateTime.now();
    }
    
    //대표 이력서로 설정
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
        if (primary) {
            updateModifiedTime();
        }
    }
    
    //공개여부설정
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        updateModifiedTime();
    }
    
    //이력서 완료 처리
    public void markAsCompleted() {
        this.status = "작성 완료";
        updateModifiedTime();
    }
    
    
    //완성도 업데이트
    public void updateCompletionRate(Short rate) {
        this.completionRate = rate;
        updateModifiedTime();
    }
}
