package boot.data.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import boot.data.entity.CompanyBookmarks;

@Repository  
public interface CompanyBookmarksRepository extends JpaRepository<CompanyBookmarks, Long> {
    
    // 기존 메소드들...
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);
    Optional<CompanyBookmarks> findByUserIdAndCompanyId(Long userId, Long companyId);
    
    // ========== 유저 관련 조회 ==========
    
    // 1. 특정 유저가 북마크한 기업 수
    Long countByUserId(Long userId);
    
    // 2. 특정 유저가 북마크한 모든 기업 목록
    List<CompanyBookmarks> findByUserId(Long userId);
    
    // 3. 특정 유저가 북마크한 기업 목록 (페이징)
    Page<CompanyBookmarks> findByUserId(Long userId, Pageable pageable);
    
    // ========== 기업 관련 조회 ==========
    
    // 4. 특정 기업을 북마크한 유저 수
    Long countByCompanyId(Long companyId);
    
    // 5. 특정 기업을 북마크한 모든 유저 목록
    List<CompanyBookmarks> findByCompanyId(Long companyId);
    
    // ========== TOP 10 기업 (북마크 수 기준) ==========
    
    //수정된 쿼리 - Object[] 형태로 반환
    @Query("SELECT cb.company, COUNT(cb) as bookmarkCount FROM CompanyBookmarks cb GROUP BY cb.company ORDER BY bookmarkCount DESC")
    List<Object[]> findTop10CompaniesByBookmarkCount(Pageable pageable);
    
    //TOP 10
    default List<Object[]> findTop10CompaniesByBookmarkCount() {
        return findTop10CompaniesByBookmarkCount(Pageable.ofSize(10));
    }
    
    // 전체 기업별 북마크 통계
    @Query("SELECT cb.company.id, COUNT(cb) FROM CompanyBookmarks cb GROUP BY cb.company.id")
    List<Object[]> getBookmarkStatsByCompany();
}