package boot.data.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import boot.data.dto.resume.ResumeProjectRequest;
import boot.data.dto.resume.ResumeProjectResponse;
import boot.data.entity.ResumeProjects;
import boot.data.entity.Resumes;
import boot.data.repository.resume.ResumeProjectsRepository;
import boot.data.repository.resume.ResumesRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ResumeProjectService {

    private final ResumesRepository resumesRepository;
    private final ResumeProjectsRepository resumeProjectsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* 공통: 이력서 소유자 확인 */
    private Resumes getOwnedResumeOrThrow(Long resumeId, Long currentUserId) {
        Resumes resume = resumesRepository.findById(resumeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다."));
        if (resume.getUser() == null || !resume.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 이력서만 접근할 수 있습니다.");
        }
        return resume;
    }

    /* JSON <-> List<String> 변환 */
    private String toJson(List<String> tech) {
        try {
            return (tech == null || tech.isEmpty()) ? null : objectMapper.writeValueAsString(tech);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "techStack 직렬화 실패");
        }
    }

    private List<String> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /* 목록 */
    @Transactional(readOnly = true)
    public List<ResumeProjectResponse> list(Long resumeId, Long currentUserId) {
        getOwnedResumeOrThrow(resumeId, currentUserId);
        return resumeProjectsRepository.findByResume_IdOrderByIdDesc(resumeId).stream()
            .map(p -> ResumeProjectResponse.builder()
                    .id(p.getId())
                    .resumeId(p.getResume().getId())
                    .projectName(p.getProjectName())
                    .organization(p.getOrganization())
                    .role(p.getRole())
                    .startDate(p.getStartDate())
                    .endDate(p.getEndDate())
                    .ongoing(p.isOngoing())
                    .projectUrl(p.getProjectUrl())
                    .description(p.getDescription())
                    .techStack(fromJson(p.getTechStack()))
                    .build()
            ).toList();
    }

    /* 생성 */
    public Long create(Long resumeId, ResumeProjectRequest req, Long currentUserId) {
        Resumes resume = getOwnedResumeOrThrow(resumeId, currentUserId);

        ResumeProjects p = new ResumeProjects();
        p.setResume(resume);
        p.setProjectName(req.getProjectName());
        p.setOrganization(req.getOrganization());
        p.setRole(req.getRole());
        p.setStartDate(req.getStartDate());
        // 진행중이면 endDate 무시
        boolean ongoing = Boolean.TRUE.equals(req.getOngoing());
        p.setOngoing(ongoing);
        p.setEndDate(ongoing ? null : req.getEndDate());
        p.setProjectUrl(req.getProjectUrl());
        p.setDescription(req.getDescription());
        p.setTechStack(toJson(req.getTechStack()));

        return resumeProjectsRepository.save(p).getId();
    }

    /* 수정 */
    public void update(Long projectId, ResumeProjectRequest req, Long currentUserId) {
        ResumeProjects p = resumeProjectsRepository.findByIdAndResume_User_Id(projectId, currentUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없거나 권한이 없습니다."));

        if (req.getProjectName() != null) p.setProjectName(req.getProjectName());
        p.setOrganization(req.getOrganization());
        p.setRole(req.getRole());
        p.setStartDate(req.getStartDate());

        boolean ongoing = Boolean.TRUE.equals(req.getOngoing());
        p.setOngoing(ongoing);
        p.setEndDate(ongoing ? null : req.getEndDate());

        p.setProjectUrl(req.getProjectUrl());
        p.setDescription(req.getDescription());
        p.setTechStack(toJson(req.getTechStack()));
        // 트랜잭션 커밋 시 자동 flush
    }

    /* 삭제 */
    public void delete(Long projectId, Long currentUserId) {
        ResumeProjects p = resumeProjectsRepository.findByIdAndResume_User_Id(projectId, currentUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없거나 권한이 없습니다."));
        resumeProjectsRepository.delete(p);
    }
}
