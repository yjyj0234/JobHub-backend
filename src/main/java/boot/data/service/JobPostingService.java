package boot.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.JobPostingCreateDto;
import boot.data.dto.JobPostingCreateDto.CategoryItem;
import boot.data.dto.JobPostingRequestDto;
import boot.data.entity.Companies;
import boot.data.entity.JobCategories;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingConditions;
import boot.data.entity.JobPostingLocations;
import boot.data.entity.JobPostings;
import boot.data.entity.Regions;
import boot.data.entity.Users;
import boot.data.repository.CompaniesRepository;
import boot.data.repository.JobCategoryRepository;
import boot.data.repository.JobPostingCategoriesRepository;
import boot.data.repository.JobPostingConditionsRepository;
import boot.data.repository.JobPostingLocationRepository;
import boot.data.repository.JobPostingsRepository;
import boot.data.repository.RegionRepository;
import boot.data.repository.UsersRepository;
import boot.data.security.CurrentUser;
import boot.data.type.CloseType;
import boot.data.type.PostingStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 주입해주는 Lombok 어노테이션
public class JobPostingService {

    private final JobPostingsRepository jobPostingsRepository;
    private final JobPostingLocationRepository jobPostingLocationRepository;
    private final JobPostingCategoriesRepository jobPostingCategoriesRepository;
    private final JobPostingConditionsRepository jobPostingConditionsRepository;

    private final CompaniesRepository companiesRepository;
    private final UsersRepository usersRepository;
    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final CurrentUser currentUser;

    /**
     * (레거시/예시) DTO를 받아 채용 공고(JobPostings)와 공고 조건(JobPostingConditions)을 저장
     */
    @Transactional
    public JobPostings createJobPosting(JobPostingRequestDto dto) {

       
        // --- 1. 부모 엔티티(JobPostings) 생성 ---
        JobPostings jobPostings = new JobPostings();

        jobPostings.setTitle(dto.getTitle());
        jobPostings.setRemote(dto.isRemote());
        jobPostings.setOpenDate(dto.getOpenDate());
        jobPostings.setCloseDate(dto.getCloseDate());

        // Enum 타입 변환
        jobPostings.setStatus(PostingStatus.valueOf(dto.getStatus().name()));
        jobPostings.setCloseType(CloseType.valueOf(dto.getCloseType().name()));

        // (필요 시) company/createdBy 설정 로직 추가

        // --- 2. 저장 ---
        JobPostings savedJobPostings = jobPostingsRepository.save(jobPostings);

        // --- 3. 조건 저장 ---
        JobPostingConditions conditions = new JobPostingConditions();
        conditions.setWorkSchedule(dto.getWorkSchedule());
        conditions.setEmploymentType(dto.getEmploymentType());
        conditions.setExperienceLevel(dto.getExperienceLevel());
        conditions.setMinExperienceYears(dto.getMinExperienceYears());
        conditions.setMaxExperienceYears(dto.getMaxExperienceYears());
        conditions.setEducationLevel(dto.getEducationLevel());
        conditions.setSalaryType(dto.getSalaryType());
        conditions.setMinSalary(dto.getMinSalary());
        conditions.setMaxSalary(dto.getMaxSalary());
        conditions.setEtc(dto.getEtc());

        // 부모 연결(@MapsId 구조일 경우 부모만 세팅)
        conditions.setJobPosting(savedJobPostings);

        jobPostingConditionsRepository.save(conditions);

        return savedJobPostings;
    }

    /**
     * 실제 사용: 생성 전용 DTO 기반으로 공고/지역/카테고리/조건 저장
     */
    @Transactional
    public Long create(JobPostingCreateDto dto) {

        //로그인 사용자 조회
        var au= currentUser.get()
                .orElseThrow(()->new SecurityException("인증 정보가 없습니다"));
        Long loginUserId=au.id();
        

        // 0-3) 회사/작성자 엔티티 로드
        Companies company = companiesRepository.findByOwner_Id(loginUserId)
        .orElseThrow(() -> new IllegalArgumentException("owner_id=" + loginUserId + " 의 회사가 없습니다."));

        Users creator = usersRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 없음: " + loginUserId));

        // --- [추가/보강] 카테고리 정규화: 중복 제거 + 대표 1개 보장 ---
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            throw new IllegalArgumentException("직무 카테고리는 1개 이상이어야 합니다.");
        }

        // 1) categoryId 기준 중복 제거(먼저 온 항목 우선, 입력 순서 보존)
        List<JobPostingCreateDto.CategoryItem> catsNormalized = new ArrayList<>(
                dto.getCategories().stream().collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                JobPostingCreateDto.CategoryItem::getCategoryId,
                                c -> c,
                                (a, b) -> a,          // 중복이면 앞의 것 유지
                                LinkedHashMap::new    // 입력 순서 보존
                        ),
                        (Map<Integer, JobPostingCreateDto.CategoryItem> m) -> new ArrayList<>(m.values())
                ))
        );

        // 2) 대표 개수 점검 → 없으면 첫 번째를 대표로, 여러 개면 첫 번째만 대표로
        long primaryCnt = catsNormalized.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsPrimary()))
                .count();

        if (primaryCnt == 0) {
            catsNormalized.get(0).setIsPrimary(true);
        } else if (primaryCnt > 1) {
            boolean keep = true;
            for (var c : catsNormalized) {
                if (Boolean.TRUE.equals(c.getIsPrimary())) {
                    if (keep) { keep = false; }
                    else { c.setIsPrimary(false); }
                }
            }
        }

        // 정규화로 교체
        dto.setCategories(catsNormalized);

        // 1) job_postings
        JobPostings posting = new JobPostings();
        posting.setCompany(company);
        posting.setTitle(dto.getTitle());
        posting.setStatus(dto.getStatus() != null ? dto.getStatus() : PostingStatus.DRAFT);
        posting.setCloseType(dto.getCloseType());
        posting.setRemote(Boolean.TRUE.equals(dto.getIsRemote()));
        posting.setOpenDate(dto.getOpenDate());
        posting.setCloseDate(dto.getCloseDate());
        posting.setCreatedBy(creator);
        posting.setCreatedAt(LocalDateTime.now());
        posting.setUpdatedAt(LocalDateTime.now());
        posting.setDescription(dto.getDescription()); 
        

        String searchText = (dto.getSearchText() != null && !dto.getSearchText().isBlank())
                ? dto.getSearchText()
                : dto.getTitle();
        posting.setSearchText(searchText);

        if (dto.getCloseType() == CloseType.UNTIL_FILLED || dto.getCloseType() == CloseType.CONTINUOUS) {
            posting.setOpenDate(null);
            posting.setCloseDate(null);
        }

        jobPostingsRepository.save(posting);

        // 2) job_posting_locations (대표 1건만 저장)
        final Integer regionId =
                (dto.getRegions() == null) ? null
                        : (dto.getRegions().getSigunguId() != null
                        ? dto.getRegions().getSigunguId()
                        : dto.getRegions().getSidoId());

        if (regionId != null) {
            Regions region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("지역 없음: " + regionId));

            JobPostingLocations loc = new JobPostingLocations();
            loc.setJobPosting(posting);
            loc.setRegion(region);
            loc.setPrimary(true);
            loc.setCreatedAt(LocalDateTime.now());

            jobPostingLocationRepository.save(loc);
        }

        // 3) job_posting_categories (N건) - 정규화된 catsNormalized 사용
        List<Integer> categoryIds = catsNormalized.stream()
                .map(CategoryItem::getCategoryId)
                .toList();

        List<JobCategories> categories = jobCategoryRepository.findByIdIn(categoryIds);

        for (CategoryItem item : catsNormalized) {
            JobCategories found = categories.stream()
                    .filter(c -> c.getId().equals(item.getCategoryId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("직무카테고리 없음: " + item.getCategoryId()));

            JobPostingCategories m = new JobPostingCategories();
            m.setJobPosting(posting);
            m.setJobCategory(found);
            m.setPrimary(Boolean.TRUE.equals(item.getIsPrimary()));
            m.setCreatedAt(LocalDateTime.now());

            jobPostingCategoriesRepository.save(m);
        }

        // 4) job_posting_conditions (1:1, @MapsId)
        JobPostingCreateDto.Conditions sc = dto.getConditions();
        if (sc == null) {
            sc = new JobPostingCreateDto.Conditions(); // NPE 방지용 기본 객체
        }

        // --- [추가/보강] 신입이면 경력 자동 보정 ---
        if (sc.getExperienceLevel() != null && "ENTRY".equals(sc.getExperienceLevel().name())) {
            sc.setMinExperienceYears(Short.valueOf((short) 0)); // ← Short로 박싱
            sc.setMaxExperienceYears(null);
        }

        JobPostingConditions cond = new JobPostingConditions();
        // 핵심: @MapsId 구조이므로 부모만 세팅(별도 postingId 세팅 X)
        cond.setJobPosting(posting);

        cond.setMinExperienceYears(sc.getMinExperienceYears());
        cond.setMaxExperienceYears(sc.getMaxExperienceYears());
        cond.setMinSalary(sc.getMinSalary());
        cond.setMaxSalary(sc.getMaxSalary());
        cond.setSalaryType(sc.getSalaryType());
        cond.setEmploymentType(sc.getEmploymentType());
        cond.setExperienceLevel(sc.getExperienceLevel());
        cond.setEducationLevel(sc.getEducationLevel());
        cond.setWorkSchedule(sc.getWorkSchedule());
        cond.setEtc(sc.getEtc());

        jobPostingConditionsRepository.save(cond);

        return posting.getId();
    }
}
