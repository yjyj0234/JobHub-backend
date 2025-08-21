// src/main/java/boot/data/service/ResumeEducationService.java
package boot.data.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.resume.EducationRequest;
import boot.data.dto.resume.EducationResponse;
import boot.data.entity.ResumeEducations;
import boot.data.entity.Resumes;
import boot.data.repository.ResumeRepository;
import boot.data.repository.resume.ResumeEducationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumeEducationService {

    private final ResumeEducationRepository educationRepository;
    private final ResumeRepository resumeRepository;

    /** 소유한 이력서 찾기 */
    private Resumes getOwnedResumeOrThrow(Long resumeId, Long userId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없거나 권한이 없습니다."));
    }

    /** 목록 (페이지) */
    @Transactional(readOnly = true)
    public Page<EducationResponse> list(Long resumeId, Long currentUserId, Pageable pageable) {
        return educationRepository
                .findByResume_IdAndResume_User_Id(resumeId, currentUserId, pageable)
                .map(EducationResponse::from);
    }

    /** 단건 조회 (소유권) */
    @Transactional(readOnly = true)
    public EducationResponse getOne(Long educationId, Long currentUserId) {
        ResumeEducations e = educationRepository
                .findByIdAndResume_User_Id(educationId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("학력 정보를 찾을 수 없거나 권한이 없습니다."));
        return EducationResponse.from(e);
    }

    /** 생성 (소유 이력서에) */
    @Transactional
    public EducationResponse create(Long resumeId, Long currentUserId, EducationRequest req) {
        Resumes resume = getOwnedResumeOrThrow(resumeId, currentUserId);

        ResumeEducations e = new ResumeEducations();
        e.setResume(resume);
        e.setSchoolName(req.schoolName());
        e.setSchoolType(req.schoolType());
        e.setMajor(req.major());
        e.setMinor(req.minor());
        e.setDegree(req.degree());
        e.setAdmissionDate(req.admissionDate());
        e.setGraduationDate(req.graduationDate());
        e.setGraduationStatus(req.graduationStatus());
        e.setGpa(req.gpa());
        e.setMaxGpa(req.maxGpa());

        resume.updateModifiedTime(); // 수정일 갱신(Resumes 엔티티에 이미 메서드 있는 상태)

        return EducationResponse.from(educationRepository.save(e));
    }

    /** 수정 (소유권) */
    @Transactional
    public EducationResponse update(Long educationId, Long currentUserId, EducationRequest req) {
        ResumeEducations e = educationRepository
                .findByIdAndResume_User_Id(educationId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("학력 정보를 찾을 수 없거나 권한이 없습니다."));

        e.setSchoolName(req.schoolName());
        e.setSchoolType(req.schoolType());
        e.setMajor(req.major());
        e.setMinor(req.minor());
        e.setDegree(req.degree());
        e.setAdmissionDate(req.admissionDate());
        e.setGraduationDate(req.graduationDate());
        e.setGraduationStatus(req.graduationStatus());
        e.setGpa(req.gpa());
        e.setMaxGpa(req.maxGpa());

        e.getResume().updateModifiedTime(); // dirty checking + 수정일 갱신
        return EducationResponse.from(e);
    }

    /** 삭제 (소유권) */
    @Transactional
    public void delete(Long educationId, Long currentUserId) {
        ResumeEducations e = educationRepository
                .findByIdAndResume_User_Id(educationId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("학력 정보를 찾을 수 없거나 권한이 없습니다."));
        e.getResume().updateModifiedTime();
        educationRepository.delete(e);
    }
}
