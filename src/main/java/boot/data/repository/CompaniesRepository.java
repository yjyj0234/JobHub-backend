package boot.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import boot.data.entity.Companies;

public interface CompaniesRepository extends JpaRepository<Companies, Long> {
    // Companies 엔티티에 대한 CRUD 기능을 자동으로 제공합니다.
    // JpaRepository를 상속받는 것만으로도 기본적인 저장(save), 조회(findById), 삭제(delete) 등의 메소드는 자동으로 구현됩니다.
    
    //소유자 ID로 기업 조회
    Optional<Companies> findByOwnerId(Long ownerId);
    
    //소유자 ID로 기업 조회 (상세정보 포함)
    @Query("SELECT c FROM Companies c LEFT JOIN FETCH c.companyDetails WHERE c.owner.id = :ownerId")
    Optional<Companies> findByOwnerIdWithDetails(@Param("ownerId") Long ownerId);
    
    // 유자 존재 여부 확인
    boolean existsByOwnerId(Long ownerId);
    
    //사업자번호 중복 체크
    boolean existsByBusinessNumber(String businessNumber);
    
    // 업자번호 중복 체크 (자신 제외)
    @Query("SELECT COUNT(c) > 0 FROM Companies c WHERE c.businessNumber = :businessNumber AND c.id != :companyId")
    boolean existsByBusinessNumberExcludingId(@Param("businessNumber") String businessNumber, @Param("companyId") Long companyId);
}
