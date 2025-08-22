package boot.data.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingLocations;
import boot.data.entity.JobPostings;

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
//지원자수 증가
      @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobPostings jp set jp.applicationCount = jp.applicationCount + 1 where jp.id = :jobId")
    int incrementApplicationCount(@Param("jobId") Long jobId);

       // -1
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobPostings jp set jp.applicationCount = case when jp.applicationCount > 0 then jp.applicationCount - 1 else 0 end where jp.id = :jobId")
    int decrementApplicationCount(@Param("jobId") Long jobId);

}