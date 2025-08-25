package boot.data.service;

import java.sql.Timestamp;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Spring Transactional
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.ApplicationCreateRequest;
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
}
