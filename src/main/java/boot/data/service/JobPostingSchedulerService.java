package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import boot.data.entity.JobPostings;
import boot.data.repository.JobPostingsRepository;
import boot.data.type.PostingStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingSchedulerService {
    
    private final JobPostingsRepository jobPostingsRepository;
    
    /**
     * 매일 자정에 실행되는 스케줄러
     * OPEN 상태이면서 마감일이 지난 공고를 EXPIRED로 변경
     */
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정
    @Transactional
    public void updateExpiredPostings() {
        log.info("채용공고 만료 상태 업데이트 시작");
        
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = jobPostingsRepository.updateExpiredPostings(now);
        
        log.info("{}개의 채용공고가 EXPIRED 상태로 변경됨", updatedCount);
    }
    
    /**
     * 더 빈번한 체크가 필요한 경우 (예: 1시간마다)
     */
    @Scheduled(fixedRate = 3600000)  // 1시간마다
    @Transactional
    public void checkAndUpdateExpiredStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // DEADLINE 타입이면서 마감일이 지난 OPEN 공고 조회
        List<JobPostings> expiredPostings = jobPostingsRepository
            .findExpiredOpenPostings(now);
        
        expiredPostings.forEach(posting -> {
            posting.setStatus(PostingStatus.EXPIRED);
            log.debug("공고 ID {} 상태를 EXPIRED로 변경", posting.getId());
        });
        
        if (!expiredPostings.isEmpty()) {
            jobPostingsRepository.saveAll(expiredPostings);
            log.info("{}개의 공고 상태 업데이트 완료", expiredPostings.size());
        }
    }
}