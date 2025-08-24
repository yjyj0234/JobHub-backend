// boot/data/service/ResumeCertificationService.java
package boot.data.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.resume.ResumeCertificationRequest.Create;
import boot.data.dto.resume.ResumeCertificationRequest.Update;
import boot.data.dto.resume.ResumeCertificationResponse;
import boot.data.entity.ResumeCertifications;
import boot.data.entity.Resumes;
import boot.data.repository.resume.ResumeCertificationsRepository;
import boot.data.repository.resume.ResumesRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ResumeCertificationService {

    private final ResumesRepository resumesRepository;
    private final ResumeCertificationsRepository resumeCertificationsRepository;

    /* 이력서 소유자 체크 */
    private Resumes getOwnedResumeOrThrow(Long resumeId, Long currentUserId) {
        Resumes resume = resumesRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다."));
        Long ownerId = resume.getUser().getId();
        if (!ownerId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 이력서만 수정할 수 있습니다.");
        }
        return resume;
    }

    @Transactional(readOnly = true)
    public List<ResumeCertificationResponse> list(Long resumeId, Long currentUserId) {
        getOwnedResumeOrThrow(resumeId, currentUserId);

        return resumeCertificationsRepository.findByResume_IdOrderByIdDesc(resumeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Long create(Long resumeId, Create req, Long currentUserId) {
        Resumes resume = getOwnedResumeOrThrow(resumeId, currentUserId);
        validateDates(req.getIssueDate(), req.getExpiryDate());

        ResumeCertifications entity = new ResumeCertifications();
        entity.setResume(resume);
        entity.setCertificationName(req.getCertificationName());
        entity.setIssuingOrganization(req.getIssuingOrganization());
        entity.setIssueDate(req.getIssueDate());
        entity.setExpiryDate(req.getExpiryDate());
        entity.setCertificationNumber(req.getCertificationNumber());

        return resumeCertificationsRepository.save(entity).getId();
    }

    public void update(Long resumeId, Long id, Update req, Long currentUserId) {
        // 소유자 검증
        getOwnedResumeOrThrow(resumeId, currentUserId);

        // 같은 이력서 소속인지 함께 확인
        ResumeCertifications entity = resumeCertificationsRepository
                .findByIdAndResume_Id(id, resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자격증 항목을 찾을 수 없습니다."));

        validateDates(req.getIssueDate(), req.getExpiryDate());

        entity.setCertificationName(req.getCertificationName());
        entity.setIssuingOrganization(req.getIssuingOrganization());
        entity.setIssueDate(req.getIssueDate());
        entity.setExpiryDate(req.getExpiryDate());
        entity.setCertificationNumber(req.getCertificationNumber());
        // JPA dirty checking 으로 반영
    }

    public void delete(Long resumeId, Long id, Long currentUserId) {
        getOwnedResumeOrThrow(resumeId, currentUserId);

        ResumeCertifications entity = resumeCertificationsRepository
                .findByIdAndResume_Id(id, resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자격증 항목을 찾을 수 없습니다."));

        resumeCertificationsRepository.delete(entity);
    }

    private void validateDates(LocalDate issueDate, LocalDate expiryDate) {
        if (issueDate != null && expiryDate != null && expiryDate.isBefore(issueDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료일은 취득일 이후여야 합니다.");
        }
    }

    private ResumeCertificationResponse toResponse(ResumeCertifications e) {
        return ResumeCertificationResponse.builder()
                .id(e.getId())
                .certificationName(e.getCertificationName())
                .issuingOrganization(e.getIssuingOrganization())
                .issueDate(e.getIssueDate())
                .expiryDate(e.getExpiryDate())
                .certificationNumber(e.getCertificationNumber())
                .build();
    }
}
