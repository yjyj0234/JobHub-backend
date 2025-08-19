// JobDetailService.java
package boot.data.service;

import java.time.Duration;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.JobDetailResponseDto;
import boot.data.entity.JobPostings;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingConditions;
import boot.data.entity.JobPostingLocations;
import boot.data.repository.JobPostingsRepository;
import boot.data.repository.JobPostingCategoriesRepository;
import boot.data.repository.JobPostingLocationRepository;
import boot.data.repository.JobPostingConditionsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobDetailService {

    private final JobPostingsRepository jobPostingsRepository;
    private final JobPostingLocationRepository jobPostingLocationRepository;
    private final JobPostingCategoriesRepository jobPostingCategoriesRepository;
    private final JobPostingConditionsRepository jobPostingConditionsRepository;

    // ★ presign 발급 서비스 주입
    private final S3StorageService s3StorageService;

    @Value("${app.s3.presign-ttl-minutes:30}")
    private long presignTtlMinutes;

    @Transactional
    public JobDetailResponseDto getDetail(Long id) {
        JobPostings posting = jobPostingsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JobPosting not found: " + id));

        posting.incrementViewCount();

        List<JobPostingLocations> locs = jobPostingLocationRepository.findByJobIdWithRegion(id);
        List<JobPostingCategories> cats = jobPostingCategoriesRepository.findByJobIdWithCategory(id);
        JobPostingConditions cond = jobPostingConditionsRepository.findById(id).orElse(null);

        // ★ 핵심: description 치환
        String replacedDesc = rewriteDescriptionWithPresigned(posting.getDescription());

        // ★ 새 오버로드 사용 (cond + 치환된 desc 같이 주입)
        return JobDetailResponseDto.from(posting, locs, cats, cond, replacedDesc);
    }

    /** DB에 저장된 description에서 <img data-s3-key="...">를 presigned URL로 교체 */
    private String rewriteDescriptionWithPresigned(String html) {
        if (html == null || html.isBlank()) return html;
        try {
            Document doc = Jsoup.parse(html);
            Elements imgs = doc.select("img[data-s3-key]");
            for (Element img : imgs) {
                String key = img.attr("data-s3-key").trim();
                if (!key.isEmpty()) {
                    String url = s3StorageService.presignGetUrl(
                            key, Duration.ofMinutes(presignTtlMinutes));
                    img.attr("src", url);
                    img.removeAttr("data-s3-key");
                    img.attr("loading", "lazy");
                    img.attr("referrerpolicy", "no-referrer");
                }
            }
            return doc.body().html();
        } catch (Exception e) {
            return html; // 실패하면 원본 반환
        }
    }
}
