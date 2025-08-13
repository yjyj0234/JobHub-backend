package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.ResumeCreateDto;
import boot.data.entity.Resumes;
import boot.data.entity.Users;
import boot.data.repository.ResumeRepository;
import boot.data.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이력서 비즈니스 로직 처리 Service
 * Controller에서 전달받은 데이터로 실제 비즈니스 로직 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {
    
    private final ResumeRepository resumeRepository;
    private final UsersRepository usersRepository;
    
    /**
     * 이력서 생성
     * @param userId 생성자 ID (JWT에서 추출)
     * @param dto 이력서 생성 데이터
     * @return 생성된 이력서 ID
     */
    @Transactional
    public Long createResume(Long userId, ResumeCreateDto dto) {
        log.info("이력서 생성 시작: userId={}, title={}", userId, dto.getTitle());
        
        // 1. 사용자 확인
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
        
        // 2. 대표 이력서 설정 시 기존 대표 해제
        if (dto.isPrimary()) {
            log.info("대표 이력서 설정 요청 - 기존 대표 이력서 해제: userId={}", userId);
            resumeRepository.clearPrimaryStatus(userId);
        }
        
        // 3. 새 이력서 생성
        Resumes resume = new Resumes();
        resume.setUser(user);
        resume.setTitle(dto.getTitle());
        resume.setPrimary(dto.isPrimary());
        resume.setPublic(dto.isPublic());
        resume.setCompletionRate((short) 10); // 제목만 입력한 상태: 10%
        resume.setStatus(dto.getStatus());
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
        
        // 4. DB 저장
        Resumes savedResume = resumeRepository.save(resume);
        
        log.info("이력서 생성 완료: resumeId={}, title={}", savedResume.getId(), savedResume.getTitle());
        return savedResume.getId();
    }
    
    /**
     * 사용자의 이력서 목록 조회
     * @param userId 사용자 ID
     * @return 이력서 목록 (최신순)
     */
    public List<Resumes> getResumesByUserId(Long userId) {
        log.info("사용자 이력서 목록 조회: userId={}", userId);
        return resumeRepository.findByUserIdOrderByIdDesc(userId);
    }
    
    /**
     * 특정 이력서 조회 (권한 체크 포함)
     * @param resumeId 이력서 ID
     * @param currentUserId 현재 사용자 ID
     * @return 이력서 정보
     * @throws IllegalArgumentException 권한이 없거나 이력서가 없는 경우
     */
    public Resumes getResumeById(Long resumeId, Long currentUserId) {
        log.info("이력서 조회: resumeId={}, userId={}", resumeId, currentUserId);
        
        return resumeRepository.findByIdAndUserId(resumeId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없거나 접근 권한이 없습니다"));
    }
    
    /**
     * 이력서 수정
     * @param resumeId 이력서 ID
     * @param currentUserId 현재 사용자 ID (권한 체크용)
     * @param dto 수정할 데이터
     * @return 수정된 이력서 ID
     */
    @Transactional
    public Long updateResume(Long resumeId, Long currentUserId, ResumeCreateDto dto) {
        log.info("이력서 수정: resumeId={}, userId={}", resumeId, currentUserId);
        
        // 1. 권한 체크 (본인 이력서인지 확인)
        Resumes resume = getResumeById(resumeId, currentUserId);
        
        // 2. 대표 이력서 설정 시 기존 대표 해제
        if (dto.isPrimary() && !resume.isPrimary()) {
            log.info("대표 이력서 변경 요청: userId={}", currentUserId);
            resumeRepository.clearPrimaryStatus(currentUserId);
        }
        
        // 3. 이력서 정보 수정
        resume.setTitle(dto.getTitle());
        resume.setPrimary(dto.isPrimary());
        resume.setPublic(dto.isPublic());
        resume.setStatus(dto.getStatus());
        resume.setUpdatedAt(LocalDateTime.now());
        
        // 4. 저장
        Resumes updatedResume = resumeRepository.save(resume);
        
        log.info("이력서 수정 완료: resumeId={}", resumeId);
        return updatedResume.getId();
    }
    
    /**
     * 이력서 삭제
     * @param resumeId 이력서 ID
     * @param currentUserId 현재 사용자 ID (권한 체크용)
     */
    @Transactional
    public void deleteResume(Long resumeId, Long currentUserId) {
        log.info("이력서 삭제: resumeId={}, userId={}", resumeId, currentUserId);
        
        // 1. 권한 체크 (본인 이력서인지 확인)
        Resumes resume = getResumeById(resumeId, currentUserId);
        
        // 2. 삭제
        resumeRepository.delete(resume);
        
        log.info("이력서 삭제 완료: resumeId={}", resumeId);
    }
    
    /**
     * 대표 이력서 설정
     * @param resumeId 대표로 설정할 이력서 ID
     * @param currentUserId 현재 사용자 ID
     */
    @Transactional
    public void setPrimaryResume(Long resumeId, Long currentUserId) {
        log.info("대표 이력서 설정: resumeId={}, userId={}", resumeId, currentUserId);
        
        // 1. 권한 체크
        Resumes resume = getResumeById(resumeId, currentUserId);
        
        // 2. 기존 대표 해제
        resumeRepository.clearPrimaryStatus(currentUserId);
        
        // 3. 새로운 대표 설정
        resume.setPrimary(true);
        resume.setUpdatedAt(LocalDateTime.now());
        resumeRepository.save(resume);
        
        log.info("대표 이력서 설정 완료: resumeId={}", resumeId);
    }
    
    /**
     * 사용자 이력서 개수 조회
     * @param userId 사용자 ID
     * @return 이력서 개수
     */
    public Long getResumeCount(Long userId) {
        return resumeRepository.countByUserId(userId);
    }
    
    /**
     * 공개된 이력서 목록 조회 (기업회원용)
     * @return 공개 이력서 목록
     */
    public List<Resumes> getPublicResumes() {
        log.info("공개 이력서 목록 조회");
        return resumeRepository.findPublicResumes();
    }
}