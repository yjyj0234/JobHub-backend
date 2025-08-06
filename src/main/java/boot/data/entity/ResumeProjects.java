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
@Table(name = "resume_projects")
public class ResumeProjects {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Resumes Entity와 ManyToOne 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resumes resume;
    
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;
    
    @Column(length = 255)
    private String organization;
    
    @Column(length = 255)
    private String role;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "is_ongoing", nullable = false)
    private boolean isOngoing = false;
    
    @Column(name = "project_url", length = 255)
    private String projectUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "tech_stack", columnDefinition = "JSON")
    private String techStack;
}