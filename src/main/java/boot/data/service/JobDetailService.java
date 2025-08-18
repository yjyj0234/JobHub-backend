package boot.data.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.JobDetailResponseDto;
import boot.data.entity.JobPostings;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingLocations;
import boot.data.repository.JobPostingsRepository;
import boot.data.repository.JobPostingLocationRepository;   // ✅ 단일 인터페이스 임포트
import boot.data.repository.JobPostingCategoriesRepository; // ✅ 단일 인터페이스 임포트
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobDetailService {

    private final JobPostingsRepository jobPostingsRepository;
    private final JobPostingLocationRepository jobPostingLocationRepository;   // ✅ 타입/이름 수정
    private final JobPostingCategoriesRepository jobPostingCategoriesRepository;

    @Transactional
    public JobDetailResponseDto getDetail(Long id) {
        JobPostings posting = jobPostingsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JobPosting not found: " + id));

        // ✅ 뷰카운트 증가 (엔티티 메서드 사용 권장)
        posting.incrementViewCount();

        // ✅ N+1 방지용 fetch join 메서드 사용 (리포지토리에 추가해둔 메서드)
        List<JobPostingLocations> locs = jobPostingLocationRepository.findByJobIdWithRegion(id);
        List<JobPostingCategories> cats = jobPostingCategoriesRepository.findByJobIdWithCategory(id);

        return JobDetailResponseDto.from(posting, locs, cats);
    }
}
