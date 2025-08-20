// src/main/java/boot/data/service/LikeService.java
package boot.data.service;

import boot.data.entity.JobPostingLikes;
import boot.data.entity.JobPostings;
import boot.data.entity.Users;
import boot.data.repository.JobPostingLikesRepository;
import boot.data.repository.JobPostingsRepository;
import boot.data.repository.UsersRepository;
import boot.data.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {
    
    private final JobPostingLikesRepository likesRepository;
    private final JobPostingsRepository jobPostingsRepository;
    private final UsersRepository usersRepository;
    private final CurrentUser currentUser;
    
    /**
     * 좋아요 토글 (추가/제거)
     */
    @Transactional
    public boolean toggleLike(Long jobPostingId) {
        Long userId = getCurrentUserId();
        
        Optional<JobPostingLikes> existing = likesRepository.findByUserIdAndJobPostingId(userId, jobPostingId);
        
        if (existing.isPresent()) {
            // 이미 좋아요됨 → 제거
            likesRepository.delete(existing.get());
            log.info("좋아요 제거: userId={}, jobPostingId={}", userId, jobPostingId);
            return false;
        } else {
            // 좋아요되지 않음 → 추가
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
            
            JobPostings jobPosting = jobPostingsRepository.findById(jobPostingId)
                    .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다: " + jobPostingId));
            
            JobPostingLikes like = new JobPostingLikes();
            like.setUser(user);
            like.setJobPosting(jobPosting);
            
            likesRepository.save(like);
            log.info("좋아요 추가: userId={}, jobPostingId={}", userId, jobPostingId);
            return true;
        }
    }
    
    /**
     * 특정 채용공고 좋아요 여부 확인
     */
    public boolean isLiked(Long userId, Long jobPostingId) {
        return likesRepository.existsByUserIdAndJobPostingId(userId, jobPostingId);
    }
    
    private Long getCurrentUserId() {
        return currentUser.get()
                .orElseThrow(() -> new SecurityException("로그인이 필요합니다"))
                .id();
    }
}