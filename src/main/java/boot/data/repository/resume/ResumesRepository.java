package boot.data.repository.resume;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import boot.data.entity.Resumes;

public interface ResumesRepository extends JpaRepository<Resumes, Long> {

    /**
     * 이력서 소유자 검증을 포함한 조회.
     * Resumes 엔티티가 User 엔티티를 'user' 필드로 가지고 있다는 전제입니다.
     */
    @Query("""
           select r
             from Resumes r
             join r.user u
            where r.id = :resumeId
              and u.id = :userId
           """)
    Optional<Resumes> findByIdAndUserId(@Param("resumeId") Long resumeId,
                                        @Param("userId") Long userId);

    /**
     * 필요 시 존재 여부만 빠르게 확인할 때 사용 가능한 파생 쿼리.
     * (서비스에서 안 쓰면 삭제해도 됩니다)
     */
    boolean existsByIdAndUser_Id(Long resumeId, Long userId);
}
