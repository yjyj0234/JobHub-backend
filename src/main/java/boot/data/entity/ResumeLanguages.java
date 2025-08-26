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
@Table(name = "resume_languages")
public class ResumeLanguages {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Resumes Entity와 ManyToOne 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resumes resume;
    
    @Column(nullable = false, length = 100)
    private String language;
    
    @Column(name = "proficiency_level", length = 50)
    private String proficiencyLevel;
    
    @Column(name = "test_name", length = 100)
    private String testName;
    
    @Column(name = "test_score", length = 50)
    private String testScore;
    
    @Column(name = "test_date")
    private LocalDate testDate;
}