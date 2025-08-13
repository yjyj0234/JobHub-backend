package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;

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



    // 실제 프로젝트에서는 Company와 Users 정보도 필요하므로 아래 Repository도 주입받아야 합니다.
    // private final CompanyRepository companyRepository;
    // private final UserRepository userRepository;

    /**
     * DTO를 받아 채용 공고(JobPostings)와 공고 조건(JobPostingConditions)을
     * 데이터베이스에 저장하는 메소드
     * @param dto 클라이언트로부터 받은 채용 공고 데이터
     * @return 생성된 JobPostings 엔티티
     */
    @Transactional // 이 메소드 내의 모든 DB 작업이 하나의 단위로 묶입니다. (All or Nothing)
    public JobPostings createJobPosting(JobPostingRequestDto dto) {
        

        Long uid = currentUser.idOrThrow();


        
        // --- 1. 부모 엔티티(JobPostings) 생성 ---
        JobPostings jobPostings = new JobPostings();
        
        // DTO에서 받은 데이터로 JobPostings 엔티티의 필드를 채웁니다.
        jobPostings.setTitle(dto.getTitle());
        jobPostings.setRemote(dto.isRemote());
        jobPostings.setOpenDate(dto.getOpenDate());
        jobPostings.setCloseDate(dto.getCloseDate());
        
        // Enum 타입 변환
        // DTO의 Enum 값을 문자열로 변환(.name())한 뒤,
        // Entity의 Enum 타입으로 다시 생성(valueOf())합니다.
        jobPostings.setStatus(PostingStatus.valueOf(dto.getStatus().name()));
        jobPostings.setCloseType(CloseType.valueOf(dto.getCloseType().name()));
        
        // [중요] 연관관계 필드(Company, Users)는 DTO에서 받은 ID를 이용해
        // 실제 엔티티를 조회한 후 설정해야 합니다. (아래는 예시 코드)
        // Companies company = companyRepository.findById(dto.getCompanyId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사입니다."));
        // Users user = userRepository.findById(dto.getCreatedById()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // jobPostings.setCompany(company);
        // jobPostings.setCreatedBy(user);


        // --- 2. 부모 엔티티를 DB에 저장 (이때 ID가 생성됩니다) ---
        JobPostings savedJobPostings = jobPostingsRepository.save(jobPostings);


        // --- 3. 자식 엔티티(JobPostingConditions) 생성 ---
        JobPostingConditions conditions = new JobPostingConditions();

        // DTO에서 받은 데이터로 JobPostingConditions 엔티티의 필드를 채웁니다.
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

        // --- 4. 부모-자식 관계 설정 ---
        // 방금 저장되어 ID가 생성된 부모(savedJobPostings)를 자식에게 연결해줍니다.
        conditions.setJobPosting(savedJobPostings);
        
        // --- 5. 자식 엔티티를 DB에 저장 ---
        jobPostingConditionsRepository.save(conditions);

        // 대표 엔티티인 JobPostings 객체를 반환합니다.
        return savedJobPostings;
    }

    @Transactional
    public Long create (JobPostingCreateDto dto){
        // 0)로드 : 회사/작성자
        Companies company=companiesRepository.findById(dto.getCompanyId())
        .orElseThrow(()->new IllegalArgumentException("회사없음: "+dto.getCompanyId()));

        Users creator= usersRepository.findById(dto.getCreatedBy())
        .orElseThrow(()->new IllegalArgumentException("작성자 없음: "+dto.getCreatedBy()));

        JobPostings posting= new JobPostings();
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

          // search_text (없으면 제목)
        String searchText = (dto.getSearchText() != null && !dto.getSearchText().isBlank())
                ? dto.getSearchText()
                : dto.getTitle();
        posting.setSearchText(searchText);

        jobPostingsRepository.save(posting); // id 생성

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
                // 3) job_posting_categories (N건)
        List<Integer> categoryIds = dto.getCategories().stream().map(CategoryItem::getCategoryId).toList();
        List<JobCategories> categories = jobCategoryRepository.findByIdIn(categoryIds);

        for (CategoryItem item : dto.getCategories()) {
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

        JobPostingConditions cond = new JobPostingConditions();
        // 핵심 수정점: @MapsId 구조이므로 postingId를 직접 세팅하지 말고 부모만 세팅!
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