// src/main/java/boot/data/service/ResumeExperienceService.java
package boot.data.service;

import boot.data.dto.resume.ExperienceRequest;
import boot.data.dto.resume.ExperienceResponse;
import boot.data.entity.Companies;
import boot.data.entity.Resumes;
import boot.data.entity.ResumeExperiences;
import boot.data.repository.resume.ResumeExperienceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResumeExperienceService {

    private final ResumeExperienceRepository repository;

    @PersistenceContext
    private EntityManager em;

    public ResumeExperienceService(ResumeExperienceRepository repository) {
        this.repository = repository;
    }

    public List<ExperienceResponse> list(Long resumeId) {
        return repository.findAllByResume_IdOrderByIdDesc(resumeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExperienceResponse create(Long resumeId, ExperienceRequest request) {
        ResumeExperiences e = new ResumeExperiences();

        // FK (resume)
        Resumes resumeRef = em.getReference(Resumes.class, resumeId);
        e.setResume(resumeRef);

        // 회사(선택)
        if (request.getCompanyId() != null) {
            e.setCompany(em.getReference(Companies.class, request.getCompanyId()));
        } else {
            e.setCompany(null);
        }

        e.setCompanyName(request.getCompanyName());
        e.setPosition(request.getPosition());
        e.setEmploymentType(request.getEmploymentType());
        e.setStartDate(request.getStartDate());
        e.setEndDate(request.getEndDate());
        e.setCurrent(request.isCurrent());     // ← setCurrent 사용
        e.setDescription(request.getDescription());
        e.setAchievements(request.getAchievements());

        repository.save(e);
        return toResponse(e);
    }

    public ExperienceResponse update(Long experienceId, ExperienceRequest request) {
        ResumeExperiences e = repository.findById(experienceId).orElseThrow();

        // 회사(선택) 갱신
        if (request.getCompanyId() != null) {
            e.setCompany(em.getReference(Companies.class, request.getCompanyId()));
        } else {
            e.setCompany(null);
        }

        e.setCompanyName(request.getCompanyName());
        e.setPosition(request.getPosition());
        e.setEmploymentType(request.getEmploymentType());
        e.setStartDate(request.getStartDate());
        e.setEndDate(request.getEndDate());
        e.setCurrent(request.isCurrent());     // ← setCurrent 사용
        e.setDescription(request.getDescription());
        e.setAchievements(request.getAchievements());

        return toResponse(e);
    }

    public void delete(Long experienceId) {
        repository.deleteById(experienceId);
    }

    private ExperienceResponse toResponse(ResumeExperiences e) {
        ExperienceResponse r = new ExperienceResponse();
        r.setId(e.getId());
        r.setCompanyName(e.getCompanyName());
        r.setCompanyId(e.getCompany() != null ? e.getCompany().getId() : null); // Long 매칭
        r.setPosition(e.getPosition());
        r.setEmploymentType(e.getEmploymentType()); // enum 그대로
        r.setStartDate(e.getStartDate());
        r.setEndDate(e.getEndDate());
        r.setCurrent(e.isCurrent());                // ← setCurrent (DTO 필드명 isCurrent)
        r.setDescription(e.getDescription());
        r.setAchievements(e.getAchievements());
        return r;
    }
}
