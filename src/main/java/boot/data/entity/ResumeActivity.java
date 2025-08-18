package boot.data.entity;

import java.time.LocalDate;

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
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "resume_activities")
public class ResumeActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("활동경력 ID")
    private Long id; // 활동경력 ID, 자동 생성

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    @Comment("이력서 ID")
    private Resumes resume; // 이력서 엔티티와 연관관계 설정

    @Column(name = "activity_name", nullable = false, length = 255)
    @Comment("활동명")
    private String activityName; // 활동명, 필수 입력

    @Column(name = "organization", length = 255)
    @Comment("활동 기관/단체")
    private String organization; // 활동 기관이나 단체명 (선택 입력)

    @Column(name = "role", length = 255)
    @Comment("역할")
    private String role; // 활동에서의 역할 (선택 입력)

    @Column(name = "start_date")
    @Comment("시작일")
    private LocalDate startDate;

    @Column(name = "end_date")
    @Comment("종료일")
    private LocalDate endDate;

    @Lob
    @Column(name = "description")
    @Comment("활동 설명")
    private String description;
}
