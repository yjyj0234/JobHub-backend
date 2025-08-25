package boot.data.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingLocations;
import boot.data.entity.JobPostings;
import boot.data.type.PostingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingsRepository extends JpaRepository<JobPostings, Long>{
	// 기본적인 저장(save), 조회(findById), 삭제(delete) 등의 메소드는
    // JpaRepository를 상속받는 것만으로도 자동으로 구현

    //회사만 즉시 로딩(컬렉션은 별도 repo에서 가져올 예정)
    @EntityGraph(attributePaths = {"company"})
    Optional<JobPostings> findById(Long id);

    public interface JobPostingLocationsRepository extends JpaRepository<JobPostingLocations, Long> {
        @Query("""
            select l
            from JobPostingLocations l
            join fetch l.region r
            where l.jobPosting.id = :jobId
            order by l.isPrimary desc, l.id asc
        """)
        List<JobPostingLocations> findByJobIdWithRegion(@Param("jobId") Long jobId);
        }

        public interface JobPostingCategoriesRepository extends JpaRepository<JobPostingCategories, Long> {
        @Query("""
            select c
            from JobPostingCategories c
            join fetch c.jobCategory cat
            where c.jobPosting.id = :jobId
            order by c.isPrimary desc, c.id asc
        """)
        List<JobPostingCategories> findByJobIdWithCategory(@Param("jobId") Long jobId);
}
@Query("""
        SELECT jp FROM JobPostings jp 
        WHERE jp.company.id = :companyId 
        ORDER BY jp.createdAt DESC
    """)
    List<JobPostings> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") Long companyId);
//지원자수 증가
      @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobPostings jp set jp.applicationCount = jp.applicationCount + 1 where jp.id = :jobId")
    int incrementApplicationCount(@Param("jobId") Long jobId);

       // -1
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobPostings jp set jp.applicationCount = case when jp.applicationCount > 0 then jp.applicationCount - 1 else 0 end where jp.id = :jobId")
    int decrementApplicationCount(@Param("jobId") Long jobId);

// 회사와 상태로 조회
List<JobPostings> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, PostingStatus status);

// 마감일이 지난 OPEN 상태 공고 조회
    @Query("""
        SELECT jp FROM JobPostings jp 
        WHERE jp.status = 'OPEN' 
        AND jp.closeType = 'DEADLINE'
        AND jp.closeDate IS NOT NULL 
        AND jp.closeDate < :now
    """)
    List<JobPostings> findExpiredOpenPostings(@Param("now") LocalDateTime now);
    
    // 한번에 업데이트 (성능 최적화)
    @Modifying
    @Query("""
        UPDATE JobPostings jp 
        SET jp.status = 'EXPIRED' 
        WHERE jp.status = 'OPEN' 
        AND jp.closeType = 'DEADLINE'
        AND jp.closeDate IS NOT NULL 
        AND jp.closeDate < :now
    """)
    int updateExpiredPostings(@Param("now") LocalDateTime now);
    
    // 진행중인 공고만 조회 (DRAFT, CLOSED, EXPIRED 제외)
    @Query("""
        SELECT jp FROM JobPostings jp 
        WHERE jp.status = 'OPEN'
        AND (jp.closeType != 'DEADLINE' 
             OR jp.closeDate IS NULL 
             OR jp.closeDate >= :now)
        ORDER BY jp.createdAt DESC
    """)
    Page<JobPostings> findActivePostings(@Param("now") LocalDateTime now, Pageable pageable);

    List<JobPostings> findByStatusOrderByCreatedAtDesc(
        PostingStatus status,
        Pageable pageable
    );

    // ✅ 회사별 진행중(OPEN) 공고 수
long countByCompanyIdAndStatus(Long companyId, PostingStatus status);

// ✅ 회사별 최근 OPEN 공고 Top 6
List<JobPostings> findTop6ByCompanyIdAndStatusOrderByCreatedAtDesc(
        Long companyId, PostingStatus status
);


}