package boot.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
    name = "resume_skills",
    uniqueConstraints = @UniqueConstraint(
        name = "ux_resume_skill_unique",
        columnNames = {"resume_id", "skill_id"}
    )
)
@Data
public class ResumeSkills {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // resume-skill 링크 PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Resumes resume; // 이미 프로젝트에 있는 이력서 엔티티 가정

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Skills skill;
}