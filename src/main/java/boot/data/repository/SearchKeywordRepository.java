// src/main/java/boot/data/repository/SearchKeywordRepository.java
package boot.data.repository;

import boot.data.entity.SearchKeywords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeywords, Long> {
    
    /**
     * 키워드로 검색어 찾기
     */
    Optional<SearchKeywords> findByKeyword(String keyword);
    
    /**
     * 인기 검색어 TOP 10
     */
    List<SearchKeywords> findTop10ByOrderBySearchCountDesc();
}