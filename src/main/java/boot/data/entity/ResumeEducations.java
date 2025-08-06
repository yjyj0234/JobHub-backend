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
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "resume_educations")
public class ResumeEducations {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Resumes Entity와 ManyToOne 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resumes resume;
    
    @Column(name = "school_name", nullable = false, length = 255)
    private String schoolName;
    
    @Column(name = "school_type", length = 50)
    private String schoolType;
    
    @Column(length = 255)
    private String major;
    
    @Column(length = 255)
    private String minor;
    
    @Column(length = 100)
    private String degree;
    
    @Column(name = "admission_date")
    private LocalDate admissionDate;
    
    @Column(name = "graduation_date")
    private LocalDate graduationDate;
    
    @Column(name = "graduation_status", length = 50)
    private String graduationStatus;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;
    
    @Column(name = "max_gpa", precision = 3, scale = 2, columnDefinition = "decimal(3,2) default 4.00")
    private BigDecimal maxGpa;
}