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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@Entity
public class Resumes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이력서 고유 ID")
    private Long id;

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
    private short completionRate = 0; // 원시 타입(널 아님)

    @Column(name = "status", nullable = false, length = 20)
    @Comment("이력서 상태 (작성 중, 작성 완료)")
    private String status = "작성 중";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Comment("수정일시")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ===== 라이프사이클 콜백: completionRate → status 자동 동기화 + 타임스탬프 =====
    @PrePersist
    private void beforeInsert() {
        syncStatusByCompletion();
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void beforeUpdate() {
        syncStatusByCompletion();
        updatedAt = LocalDateTime.now();
    }

    /** completion_rate에 맞춰 status를 자동 세팅 */
    private void syncStatusByCompletion() {
        int rate = this.completionRate; // 원시 short이므로 null 없음
        this.status = (rate >= 100) ? "작성 완료" : "작성 중";
    }

    // ========== 편의 메서드(타임스탬프는 콜백에서 처리되므로 여기선 설정만) ==========
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /** 완성도 업데이트(0~100 클램핑) */
    public void updateCompletionRate(Short rate) {
        int r = (rate == null) ? 0 : rate.intValue();
        if (r < 0) r = 0;
        if (r > 100) r = 100;
        this.completionRate = (short) r;
    }

    /** 완료 처리: 규칙과 충돌하지 않게 completionRate=100으로 맞춤 */
    public void markAsCompleted() {
        this.completionRate = 100;
        // status는 @PreUpdate에서 자동 반영
    }
    public void updateModifiedTime() {
        this.updatedAt = LocalDateTime.now();
    }
}
