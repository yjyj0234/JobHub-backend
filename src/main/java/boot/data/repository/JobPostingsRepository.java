package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import boot.data.entity.JobPostings;
public interface JobPostingsRepository extends JpaRepository<JobPostings, Long>{
	// 기본적인 저장(save), 조회(findById), 삭제(delete) 등의 메소드는
    // JpaRepository를 상속받는 것만으로도 자동으로 구현
}