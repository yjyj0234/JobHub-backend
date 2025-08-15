package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.resume.ActivityRequest;
import boot.data.dto.resume.ActivityResponse;
import boot.data.dto.resume.ResumeCreateDto; // ← 패키지 위치 너가 쓴 곳과 동일하게 유지
import boot.data.entity.ResumeActivity;
import boot.data.entity.Resumes;
import boot.data.entity.Users;
import boot.data.repository.ResumeRepository;
import boot.data.repository.UsersRepository;
import boot.data.repository.resume.ResumeActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이력서 비즈니스 로직
 * - Resumes CRUD
 * - resume_activities CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UsersRepository usersRepository;
    private final ResumeActivityRepository resumeActivityRepository;

    // ===================== Resumes =====================

    /** 이력서 생성 */
    @Transactional
    public Long createResume(Long userId, ResumeCreateDto dto) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    
        if (dto.isPrimary()) {
            resumeRepository.clearPrimaryStatus(userId);
        }
    
        Resumes resume = new Resumes();
        resume.setUser(user);
        resume.setTitle(dto.getTitle());
        resume.setPrimary(dto.isPrimary());
        resume.setPublic(dto.isPublic());
    
        short rate = dto.getCompletionRate() == null ? 0 : dto.getCompletionRate();
        resume.setCompletionRate(rate);
        // ✅ 완성도 기준으로 상태 자동 결정
        resume.setStatus(rate >= 100 ? "작성 완료" : "작성 중");
    
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
    
        return resumeRepository.save(resume).getId();
    }

    /** 사용자 이력서 목록(최신순) */
    @Transactional(readOnly = true)
    public List<Resumes> getResumesByUserId(Long userId) {
        log.info("사용자 이력서 목록 조회: userId={}", userId);
        return resumeRepository.findByUserIdOrderByIdDesc(userId);
    }

    /** 이력서 단건 조회(소유권 확인) */
    @Transactional(readOnly = true)
    public Resumes getResumeById(Long resumeId, Long currentUserId) {
        return resumeRepository.findByIdAndUserId(resumeId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없거나 접근 권한이 없습니다"));
    }

    /** 이력서 수정(소유권 확인) */
    @Transactional
public Long updateResume(Long resumeId, Long currentUserId, ResumeCreateDto dto) {
    Resumes resume = getResumeById(resumeId, currentUserId);

    if (dto.isPrimary() && !resume.isPrimary()) {
        resumeRepository.clearPrimaryStatus(currentUserId);
    }

    resume.setTitle(dto.getTitle());
    resume.setPrimary(dto.isPrimary());
    resume.setPublic(dto.isPublic());

    // ✅ 업데이트 시에도 완성도 반영 + 상태 자동 결정
    short rate = dto.getCompletionRate() == null ? resume.getCompletionRate() : dto.getCompletionRate();
    resume.setCompletionRate(rate);
    resume.setStatus(rate >= 100 ? "작성 완료" : "작성 중");

    resume.setUpdatedAt(LocalDateTime.now());
    return resumeRepository.save(resume).getId();
}

    /** 이력서 삭제(소유권 확인) */
    @Transactional
    public void deleteResume(Long resumeId, Long currentUserId) {
        log.info("이력서 삭제: resumeId={}, userId={}", resumeId, currentUserId);
        Resumes resume = getResumeById(resumeId, currentUserId);
        resumeRepository.delete(resume);
    }

    /** 대표 이력서 설정(소유권 확인) */
    @Transactional
    public void setPrimaryResume(Long resumeId, Long currentUserId) {
        log.info("대표 이력서 설정: resumeId={}, userId={}", resumeId, currentUserId);

        Resumes resume = getResumeById(resumeId, currentUserId);
        resumeRepository.clearPrimaryStatus(currentUserId);
        resume.setPrimary(true);
        resume.setUpdatedAt(LocalDateTime.now());
        resumeRepository.save(resume);

        log.info("대표 이력서 설정 완료: resumeId={}", resumeId);
    }

    /** 사용자 이력서 개수 */
    @Transactional(readOnly = true)
    public Long getResumeCount(Long userId) {
        return resumeRepository.countByUserId(userId);
    }

    /** 공개 이력서 목록 */
    @Transactional(readOnly = true)
    public List<Resumes> getPublicResumes() {
        log.info("공개 이력서 목록 조회");
        return resumeRepository.findPublicResumes();
    }

    // ===================== Activities (resume_activities) =====================

    /** 소유권 검사용 헬퍼 */
    private Resumes getOwnedResumeOrThrow(Long resumeId, Long userId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없거나 권한이 없습니다."));
    }

    /** 활동 목록 (해당 이력서 + 본인 소유) */
    @Transactional(readOnly = true)
    public Page<ActivityResponse> listActivities(Long resumeId, Long currentUserId, Pageable pageable) {
        return resumeActivityRepository
                .findByResume_IdAndResume_User_Id(resumeId, currentUserId, pageable)
                .map(ActivityResponse::from);
    }

    /** 활동 단건 조회 (본인 소유) — 컨트롤러 시그니처와 동일 */
    @Transactional(readOnly = true)
    public ActivityResponse getActivity(Long activityId, Long currentUserId) {
        ResumeActivity entity = resumeActivityRepository
                .findByIdAndResume_User_Id(activityId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없거나 권한이 없습니다."));
        return ActivityResponse.from(entity);
    }

    /** 활동 생성 (해당 이력서 + 본인 소유) */
    @Transactional
    public ActivityResponse createActivity(Long resumeId, Long currentUserId, ActivityRequest req) {
        Resumes resume = getOwnedResumeOrThrow(resumeId, currentUserId);

        ResumeActivity entity = new ResumeActivity();
        entity.setResume(resume);
        entity.setActivityName(req.activityName());
        entity.setOrganization(req.organization());
        entity.setRole(req.role());
        entity.setStartDate(req.startDate());
        entity.setEndDate(req.endDate());
        entity.setDescription(req.description());

        resume.updateModifiedTime();

        return ActivityResponse.from(resumeActivityRepository.save(entity));
    }

    /** 활동 수정 (본인 소유) */
    @Transactional
    public ActivityResponse updateActivity(Long activityId, Long currentUserId, ActivityRequest req) {
        ResumeActivity entity = resumeActivityRepository
                .findByIdAndResume_User_Id(activityId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없거나 권한이 없습니다."));

        entity.setActivityName(req.activityName());
        entity.setOrganization(req.organization());
        entity.setRole(req.role());
        entity.setStartDate(req.startDate());
        entity.setEndDate(req.endDate());
        entity.setDescription(req.description());

        entity.getResume().updateModifiedTime();

        return ActivityResponse.from(entity); // dirty checking
    }

    /** 활동 삭제 (본인 소유) */
    @Transactional
    public void deleteActivity(Long activityId, Long currentUserId) {
        ResumeActivity entity = resumeActivityRepository
                .findByIdAndResume_User_Id(activityId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없거나 권한이 없습니다."));

        entity.getResume().updateModifiedTime();
        resumeActivityRepository.delete(entity);
    }
}
