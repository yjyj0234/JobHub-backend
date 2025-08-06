package boot.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "resume_awards")
public class ResumeAwards {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ManyToOne 관계 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resumes resume;
    
    @Column(name = "award_name", nullable = false, length = 255)
    private String awardName;
    
    @Column(nullable = false, length = 255)
    private String organization;
    
    @Column(name = "award_date", nullable = false)
    private LocalDate awardDate;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}