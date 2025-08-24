// src/main/java/boot/data/service/ResumeLanguageService.java
package boot.data.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.resume.ResumeLanguageDto;
import boot.data.entity.ResumeLanguages;
import boot.data.entity.Resumes;
import boot.data.repository.resume.ResumeLanguagesRepository;
import boot.data.repository.resume.ResumesRepository;
import boot.data.security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeLanguageService {

    private final ResumeLanguagesRepository languagesRepo;
    private final ResumesRepository resumesRepo;
    private final CurrentUser currentUser;

    // 내 이력서 소유권 검증 + 로드
    private Resumes loadMyResume(Long resumeId) {
        Long uid = currentUser.idOrThrow();
        Resumes resume = resumesRepo.findById(resumeId)
                .orElseThrow(() -> new EntityNotFoundException("이력서를 찾을 수 없습니다. id=" + resumeId));

        // Resumes 엔티티에 getUser().getId() 형태로 소유자 접근 가능하다고 가정
        Long ownerId = resume.getUser().getId();
        if (!uid.equals(ownerId)) {
            throw new AccessDeniedException("해당 이력서에 대한 권한이 없습니다.");
        }
        return resume;
    }

    // 목록
    public List<ResumeLanguageDto> list(Long resumeId) {
        loadMyResume(resumeId); // 권한 체크만
        return languagesRepo.findByResumeIdOrderByIdAsc(resumeId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public ResumeLanguageDto create(Long resumeId, ResumeLanguageDto dto) {
        Resumes resume = loadMyResume(resumeId);

        ResumeLanguages entity = new ResumeLanguages();
        entity.setResume(resume);
        apply(dto, entity);

        ResumeLanguages saved = languagesRepo.save(entity);
        return toDto(saved);
    }

    @Transactional
    public ResumeLanguageDto update(Long resumeId, Long id, ResumeLanguageDto dto) {
        loadMyResume(resumeId); // 권한 체크
        ResumeLanguages entity = languagesRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("외국어 항목을 찾을 수 없습니다. id=" + id));

        // URL의 resumeId와 실제 엔티티의 resumeId가 일치하는지 방어
        if (!entity.getResume().getId().equals(resumeId)) {
            throw new AccessDeniedException("잘못된 접근입니다.");
        }

        apply(dto, entity);
        return toDto(entity);
    }

    @Transactional
    public void delete(Long resumeId, Long id) {
        loadMyResume(resumeId); // 권한 체크
        // 존재 + 소유 이력서 항목인지 확인
        boolean ok = languagesRepo.existsByIdAndResumeId(id, resumeId);
        if (!ok) throw new EntityNotFoundException("외국어 항목을 찾을 수 없습니다. id=" + id);

        languagesRepo.deleteByIdAndResumeId(id, resumeId);
    }

    /* ---------- mapper ---------- */

    private ResumeLanguageDto toDto(ResumeLanguages e) {
        return ResumeLanguageDto.builder()
                .id(e.getId())
                .language(e.getLanguage())
                .proficiencyLevel(e.getProficiencyLevel())
                .testName(e.getTestName())
                .testScore(e.getTestScore())
                .testDate(e.getTestDate())
                .build();
    }

    private void apply(ResumeLanguageDto dto, ResumeLanguages e) {
        e.setLanguage(dto.getLanguage());
        e.setProficiencyLevel(dto.getProficiencyLevel());
        e.setTestName(dto.getTestName());
        e.setTestScore(dto.getTestScore());
        e.setTestDate(dto.getTestDate());
    }
}
