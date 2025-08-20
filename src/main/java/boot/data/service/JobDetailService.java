// src/main/java/boot/data/service/JobDetailService.java
package boot.data.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.JobDetailResponseDto;
import boot.data.entity.JobPostings;
import boot.data.entity.Regions;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingConditions;
import boot.data.entity.JobPostingLocations;
import boot.data.repository.JobPostingsRepository;
// import boot.data.repository.RegionRepository; // ← 필요 없음
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
    // private final RegionRepository regionRepository; // ← 삭제 가능

    // S3 presign 발급
    private final S3StorageService s3StorageService;

    @Value("${app.s3.presign-ttl-minutes:30}")
    private long presignTtlMinutes;

    /**
     * 채용공고 상세
     */
    @Transactional
    public JobDetailResponseDto getDetail(Long id) {
        JobPostings posting = jobPostingsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JobPosting not found: " + id));

        // 조회수 +1
        posting.incrementViewCount();

        // 연관 데이터 조회 (region + region.parent 를 JOIN FETCH로 끌어오면 N+1 방지됨)
        List<JobPostingLocations> locs = jobPostingLocationRepository.findByJobIdWithRegion(id);
        List<JobPostingCategories> cats = jobPostingCategoriesRepository.findByJobIdWithCategory(id);
        JobPostingConditions cond = jobPostingConditionsRepository.findById(id).orElse(null);

        // “부모 + 자식” 합성된 LocationDto 리스트 생성
        List<JobDetailResponseDto.LocationDto> locationDtos = locs.stream()
                .map(l -> {
                    Regions r = l.getRegion();
                    String child = (r != null && r.getName() != null) ? r.getName() : "";
                    Regions p = (r != null) ? r.getParent() : null;
                    String parent = (p != null && p.getName() != null) ? p.getName() : "";
                    String full = parent.isEmpty() ? child : (parent + " " + child);

                    return JobDetailResponseDto.LocationDto.builder()
                            .regionId(r != null ? r.getId() : null)
                            .name(full)
                            .isPrimary(l.isPrimary())
                            .build();
                })
                .collect(Collectors.toList());

        // description 내 data-s3-key → presigned URL 치환
        String replacedDesc = rewriteDescriptionWithPresigned(posting.getDescription());

        // 합성된 지역명을 사용하는 팩토리 호출
        return JobDetailResponseDto.fromResolvedLocations(
                posting,
                locationDtos,
                cats,
                cond,
                replacedDesc
        );
    }

    /**
     * DB에 저장된 description에서 <img data-s3-key="...">를 presigned URL로 교체
     */
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
            // 실패 시 원본 반환
            return html;
        }
    }
}
