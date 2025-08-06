package boot.data.dto;

import org.hibernate.annotations.Comment;

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
	
	public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, INTERN, FREELANCE
	}
	 // 경력
    public enum ExperienceLevel {
        ENTRY, JUNIOR, MID, SENIOR, LEAD, EXECUTIVE
    }
    //학력 COLLEGE:전문대학 졸업,UNIVERSITY:4년제 대학교 졸업
    //MASTER:대학원 석사 졸업,PHD:박사 학위 보유
    public enum EducationLevel {
        ANY, HIGH_SCHOOL, COLLEGE, UNIVERSITY, MASTER, PHD
    }
    
    // 급여 유형을 나타내는 Enum
    //NEGOTIABLE	협의 가능 (Salary to be negotiated) 면접 후 결정 등
    //UNDISCLOSED	비공개 (Salary not disclosed) 급여 정보를 공개하지 않음
    public enum SalaryType {
        ANNUAL, MONTHLY, HOURLY, NEGOTIABLE, UNDISCLOSED
    }
    
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
