package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.Companies;

public interface CompaniesRepository extends JpaRepository<Companies, Long> {
    // Companies 엔티티에 대한 CRUD 기능을 자동으로 제공합니다.
    // JpaRepository를 상속받는 것만으로도 기본적인 저장(save), 조회(findById), 삭제(delete) 등의 메소드는 자동으로 구현됩니다.
    
}
