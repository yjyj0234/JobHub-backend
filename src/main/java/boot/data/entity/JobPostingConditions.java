package boot.data.entity;

import org.hibernate.annotations.Comment;

import boot.data.type.EducationLevel;
import boot.data.type.EmploymentType;
import boot.data.type.ExperienceLevel;
import boot.data.type.SalaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "job_posting_conditions")
public class JobPostingConditions {
	
    @Id
    @Column(name = "posting_id")
    private Long postingId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId	//부모 엔티티의 PK를 자식 엔티티의 PK로 그대로 쓰는 1:1 관계에서 사용
    @JoinColumn(name = "posting_id")
    private JobPostings jobPosting;
    
    @Column(name = "work_schedule",nullable = false)
    private String workSchedule;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type",nullable = false)
    @Comment("고용 형태")
    private EmploymentType employmentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level",nullable = false)
    private ExperienceLevel experienceLevel;
    
    @Column(name = "min_experience_years", nullable = false)
    @Comment("최소 경력 년수")
    private Short minExperienceYears=0;
    
    @Column(name = "max_experience_years")
    @Comment("최대 경력 년수")
    private Short maxExperienceYears;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false)
    @Comment("학력 요구사항")
    private EducationLevel educationLevel = EducationLevel.ANY;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type", nullable = false)
    @Comment("급여 유형")
    private SalaryType salaryType;
    
    @Column(name = "min_salary")
    @Comment("최소 급여")
    private Integer minSalary;

    @Column(name = "max_salary")
    @Comment("최대 급여")
    private Integer maxSalary;

    @Column(nullable = false, length = 255)
    @Comment("우대사항")
    private String etc;
}
