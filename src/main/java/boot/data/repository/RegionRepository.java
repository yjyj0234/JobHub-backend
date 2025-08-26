package boot.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import boot.data.entity.Regions;

@Repository
public interface RegionRepository extends JpaRepository<Regions, Integer> {
	
	//레벨로 조회 (최상위)
    List<Regions> findByLevel(Short level);
    
    //부모 id로 하위 조회(parent_Id)
    List<Regions> findByParentId(Integer parentId);
    
    // 레벨 안 쓰고 최상위만 뽑고 싶을 때 대안
    List<Regions> findByParentIsNull();

    // 정렬이 필요하면 이렇게도 사용 가능
    List<Regions> findAllByOrderByIdAsc();

    //정확 일치 보조(혹시 컬레이션 때문에 IgnoreCase가 기대와 다를 때 백업).
    Optional<Regions> findFirstByName(String name);

    //사용자 입력 오타(대소문자)와 DB 설정 차이를 흡수.
    Optional<Regions> findFirstByNameIgnoreCase(String name);

}