// src/main/java/boot/data/service/ResumePortfolioService.java
package boot.data.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.resume.ResumePortfolioDto;
import boot.data.entity.ResumePortfolios;
import boot.data.entity.Resumes;
import boot.data.repository.resume.ResumePortfoliosRepository;
import boot.data.repository.resume.ResumesRepository;
import boot.data.security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumePortfolioService {

    private final ResumePortfoliosRepository portfoliosRepo;
    private final ResumesRepository resumesRepo;
    private final CurrentUser currentUser;

    private Resumes loadMyResume(Long resumeId) {
        Long uid = currentUser.idOrThrow();
        Resumes resume = resumesRepo.findById(resumeId)
            .orElseThrow(() -> new EntityNotFoundException("이력서를 찾을 수 없습니다. id=" + resumeId));
        Long ownerId = resume.getUser().getId();
        if (!uid.equals(ownerId)) throw new AccessDeniedException("해당 이력서에 대한 권한이 없습니다.");
        return resume;
    }

    private ResumePortfolios loadMyPortfolio(Long id) {
        Long uid = currentUser.idOrThrow();
        ResumePortfolios e = portfoliosRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("포트폴리오를 찾을 수 없습니다. id=" + id));
        Long ownerId = e.getResume().getUser().getId();
        if (!uid.equals(ownerId)) throw new AccessDeniedException("해당 항목에 대한 권한이 없습니다.");
        return e;
    }

    public List<ResumePortfolioDto> list(Long resumeId) {
        loadMyResume(resumeId); // 권한 체크
        return portfoliosRepo.findByResumeIdOrderByIdAsc(resumeId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public ResumePortfolioDto create(Long resumeId, ResumePortfolioDto dto) {
        Resumes resume = loadMyResume(resumeId);
        ResumePortfolios e = new ResumePortfolios();
        e.setResume(resume);
        apply(dto, e);
        return toDto(portfoliosRepo.save(e));
    }

    @Transactional
    public ResumePortfolioDto update(Long id, ResumePortfolioDto dto) {
        ResumePortfolios e = loadMyPortfolio(id);
        apply(dto, e);
        return toDto(e);
    }

    @Transactional
    public void delete(Long id) {
        ResumePortfolios e = loadMyPortfolio(id);
        portfoliosRepo.delete(e);
    }

    /* ---------- mapper ---------- */

    private ResumePortfolioDto toDto(ResumePortfolios e) {
        return ResumePortfolioDto.builder()
            .id(e.getId())
            .title(e.getTitle())
            .url(e.getUrl())
            .description(e.getDescription())
            .portfolioType(e.getPortfolioType())
            .build();
    }

    private void apply(ResumePortfolioDto d, ResumePortfolios e) {
        e.setTitle(d.getTitle());
        e.setUrl(d.getUrl());
        e.setDescription(d.getDescription());
        e.setPortfolioType(d.getPortfolioType());
    }
}
