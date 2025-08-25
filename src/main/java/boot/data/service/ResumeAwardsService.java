// src/main/java/boot/data/service/resume/ResumeAwardsService.java
package boot.data.service;

import boot.data.dto.resume.ResumeAwardRequest;
import boot.data.dto.resume.ResumeAwardResponse;
import boot.data.entity.ResumeAwards;
import boot.data.entity.Resumes;
import boot.data.repository.resume.ResumeAwardsRepository;
import boot.data.repository.resume.ResumesRepository;
import boot.data.security.CurrentUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ResumeAwardsService {

    private final ResumeAwardsRepository awardsRepo;
    private final ResumesRepository resumesRepo;
    private final CurrentUser currentUser;

    public ResumeAwardsService(
            ResumeAwardsRepository awardsRepo,
            ResumesRepository resumesRepo,
            CurrentUser currentUser
    ) {
        this.awardsRepo = awardsRepo;
        this.resumesRepo = resumesRepo;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<ResumeAwardResponse> listByResumeId(Long resumeId) {
        Resumes resume = assertResumeOwner(resumeId);
        return awardsRepo.findAllByResume_IdOrderByIdDesc(resume.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Long create(Long resumeId, ResumeAwardRequest.Create dto) {
        Resumes resume = assertResumeOwner(resumeId);
        ResumeAwards a = new ResumeAwards();
        a.setResume(resume);
        a.setAwardName(dto.getAwardName());
        a.setOrganization(dto.getOrganization());
        a.setAwardDate(dto.getAwardDate());
        a.setDescription(dto.getDescription());
        awardsRepo.save(a);
        return a.getId();
    }

    public void update(Long resumeId, Long awardId, ResumeAwardRequest.Update dto) {
        assertResumeOwner(resumeId);
        ResumeAwards a = awardsRepo.findByIdAndResume_Id(awardId, resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수상 이력을 찾을 수 없습니다."));
        a.setAwardName(dto.getAwardName());
        a.setOrganization(dto.getOrganization());
        a.setAwardDate(dto.getAwardDate());
        a.setDescription(dto.getDescription());
    }

    public void delete(Long resumeId, Long awardId) {
        Long userId = currentUser.idOrThrow();
        if (!awardsRepo.existsByIdAndResume_User_Id(awardId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 수상 이력이 없습니다.");
        }
        awardsRepo.deleteById(awardId);
    }

    private ResumeAwardResponse toResponse(ResumeAwards a) {
        return new ResumeAwardResponse(
                a.getId(),
                a.getAwardName(),
                a.getOrganization(),
                a.getAwardDate(),
                a.getDescription()
        );
    }

    private Resumes assertResumeOwner(Long resumeId) {
        Long userId = currentUser.idOrThrow();
        Resumes resume = resumesRepo.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다."));
        if (resume.getUser() == null || !userId.equals(resume.getUser().getId())) {
            throw new AccessDeniedException("권한이 없어요.");
        }
        return resume;
    }
}
