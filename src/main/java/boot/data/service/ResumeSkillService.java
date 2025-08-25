// src/main/java/boot/data/service/ResumeSkillService.java
package boot.data.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.resume.ResumeSkillResponse;
import boot.data.entity.ResumeSkills;
import boot.data.entity.Resumes;
import boot.data.entity.Skills;
import boot.data.repository.resume.ResumeSkillsRepository;
import boot.data.repository.resume.ResumesRepository;
import boot.data.repository.resume.SkillsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ResumeSkillService {

    private final ResumesRepository resumesRepository;
    private final ResumeSkillsRepository resumeSkillsRepository;
    private final SkillsRepository skillsRepository;

    /* 이력서 소유자 체크 */
    private Resumes getOwnedResumeOrThrow(Long resumeId, Long currentUserId) {
        Resumes resume = resumesRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다."));
        Long ownerId = resume.getUser().getId();
        if (!Objects.equals(ownerId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 이력서만 수정할 수 있습니다.");
        }
        return resume;
    }

    @Transactional(readOnly = true)
    public List<ResumeSkillResponse> list(Long resumeId, Long currentUserId) {
        getOwnedResumeOrThrow(resumeId, currentUserId);
        return resumeSkillsRepository.findByResume_Id(resumeId).stream()
                .map(rs -> ResumeSkillResponse.builder()
                        .id(rs.getId()) // resume_skills PK
                        .skillId(rs.getSkill().getId())
                        .name(rs.getSkill().getName())
                        .categoryId(rs.getSkill().getCategoryId())
                        .isVerified(rs.getSkill().isVerified())
                        .build())
                .toList();
    }

    public Long link(Long resumeId, Integer skillId, Long currentUserId) {
        Resumes resume = getOwnedResumeOrThrow(resumeId, currentUserId);
        Skills skill = skillsRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스킬을 찾을 수 없습니다."));

        if (resumeSkillsRepository.existsByResume_IdAndSkill_Id(resumeId, skillId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 추가된 스킬입니다.");
        }

        ResumeSkills link = new ResumeSkills();
        link.setResume(resume);
        link.setSkill(skill);
        return resumeSkillsRepository.save(link).getId();
    }

    public void unlink(Long resumeId, Long resumeSkillId, Long currentUserId) {
        // 권한 + 소속 이력서 검증
        ResumeSkills link = resumeSkillsRepository
                .findByIdAndResume_IdAndResume_User_Id(resumeSkillId, resumeId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "링크가 없거나 권한이 없습니다."));

        Integer skillId = link.getSkill() != null ? link.getSkill().getId() : null;

        // 링크만 삭제
        resumeSkillsRepository.delete(link);

        // 고아 스킬 정리: 비검증 && 더 이상 어떤 이력서에도 연결 X
        if (skillId != null && resumeSkillsRepository.countBySkill_Id(skillId) == 0) {
            skillsRepository.findById(skillId).ifPresent(skill -> {
                if (!skill.isVerified()) {
                    skillsRepository.delete(skill);
                }
            });
        }
    }
}
