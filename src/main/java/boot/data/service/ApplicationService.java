package boot.data.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Spring Transactional
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.ApplicationCreateRequest;
import boot.data.dto.ApplicationResponse;
import boot.data.entity.Applications;
import boot.data.entity.JobPostings;
import boot.data.entity.Resumes;
import boot.data.entity.Users;
import boot.data.repository.ApplicationRepository;
import boot.data.repository.JobPostingsRepository;
import boot.data.repository.ResumeRepository;
import boot.data.repository.UsersRepository;
import boot.data.type.ApplicationStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationsRepository;
    private final JobPostingsRepository jobPostingsRepository;
    private final ResumeRepository resumeRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public Applications apply(Long userId, ApplicationCreateRequest req) {
        if (req.getJobId() == null || req.getResumeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobId, resumeId는 필수입니다.");
        }

        // 1) 공고 존재 확인
        JobPostings posting = jobPostingsRepository.findById(req.getJobId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 공고가 존재하지 않습니다."));

        // (선택) 공고 상태/마감 체크
        // if (posting.getStatus() != JobStatus.OPEN) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT, "마감되었거나 비공개 공고입니다.");
        // }

        // 2) 본인 이력서인지 검증
        Resumes resume = resumeRepository.findByIdAndUserId(req.getResumeId(), userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 이력서만 사용할 수 있습니다."));

        // 3) 중복지원 방지(애플리케이션 레벨)
        if (applicationsRepository.existsByUser_IdAndJobPosting_Id(userId, posting.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 공고에 지원했습니다.");
        }

        // 4) 저장
        Users userRef = usersRepository.getReferenceById(userId);
        Applications app = new Applications();
        app.setUser(userRef);
        app.setJobPosting(posting);
        app.setResume(resume);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(new Timestamp(System.currentTimeMillis()));
        app.setViewedAt(null);

        try {
            Applications saved = applicationsRepository.save(app);

            // 5) 지원자 수 +1 (DB 원자 증가, @Modifying 필요)
            jobPostingsRepository.incrementApplicationCount(posting.getId());

            return saved;
        } catch (DataIntegrityViolationException dup) {
            // (권장) DB에 UNIQUE (user_id, posting_id) 제약 걸었다면 동시요청시 여기로 들어올 수 있음
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 공고에 지원했습니다.");
        }
    }
 // ✅ 열람 처리: viewed_at 갱신, APPLIED -> VIEWED
    @Transactional
    public void markViewedByCompany(Long companyUserId, Long applicationId) {
        Applications app = applicationsRepository.findById(applicationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지원 기록이 없습니다."));

    JobPostings posting = app.getJobPosting();
    // ✅ 여기 소유권 검증 추가
    assertOwnedByCompanyUser(posting, companyUserId);

    app.setViewedAt(Timestamp.from(Instant.now()));
    if (app.getStatus() == ApplicationStatus.APPLIED) {
        app.setStatus(ApplicationStatus.VIEWED);
    }
    applicationsRepository.save(app);
    }

    // ✅ 상태 변경
    @Transactional
    public void changeStatusByCompany(Long companyUserId, Long applicationId, String nextStatus) {
        Applications app = applicationsRepository.findById(applicationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지원 기록이 없습니다."));

            JobPostings posting = app.getJobPosting();
            // ✅ 여기 소유권 검증 추가
            assertOwnedByCompanyUser(posting, companyUserId);

            ApplicationStatus ns;
            try {
                ns = ApplicationStatus.valueOf(nextStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다.");
            }
            app.setStatus(ns);
            applicationsRepository.save(app);
    }

    private void assertOwnedByCompanyUser(JobPostings posting, Long companyUserId) {
        // 1순위: 회사 소유자(대표) 비교
        Long ownerUserId = posting.getCompany().getOwner() != null
                ? posting.getCompany().getOwner().getId()
                : null;
    
        // (옵션) 작성자도 회사 계정이라면 보조 키로 허용하고 싶으면 아래 주석 해제
        Long createdById = posting.getCreatedBy() != null
                ? posting.getCreatedBy().getId()
                : null;
    
        boolean ok = false;
        if (ownerUserId != null && ownerUserId.equals(companyUserId)) ok = true;
        // 작성자도 소유자로 인정하려면 주석 해제
        // if (!ok && createdById != null && createdById.equals(companyUserId)) ok = true;
    
        if (!ok) {
            throw new AccessDeniedException("해당 회사의 공고가 아닙니다.");
        }
    }
   @Transactional(readOnly = true)
    public List<ApplicationResponse> findByPostingIdForCompany(Long companyUserId, Long postingId) {

       // 1) 공고 + 회사 소유자 로딩 후 소유권 검증
       var posting = jobPostingsRepository.findByIdWithCompanyOwner(postingId)
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공고가 존재하지 않습니다."));
       assertOwnedByCompanyUser(posting, companyUserId);

       // 2) 조인 결과 한번에 조회
       var rows = applicationsRepository.findListRowsByPostingId(postingId);

        return rows.stream().map(r ->
            ApplicationResponse.builder()
                .id(r.getId())
                .postingId(r.getPostingId())
                .resumeId(r.getResumeId())
                .userId(r.getUserId())
                .status(ApplicationStatus.valueOf(r.getStatus()))
                .appliedAt(r.getAppliedAt())
                .viewedAt(r.getViewedAt())
                .applicantName(r.getApplicantName())
                .applicantEmail(r.getApplicantEmail())
                .resumeTitle(r.getResumeTitle())
                .resumeUrl(r.getResumePortfolioUrl()) // 포트폴리오 URL이 있으면 사용
                .build()
        ).toList();
    }
 
}
