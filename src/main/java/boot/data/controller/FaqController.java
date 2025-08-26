// JobHub-backend/src/main/java/boot/data/controller/FaqController.java

package boot.data.controller;

// 필요한 클래스들을 임포트합니다.
import boot.data.dto.FaqCreateRequest;
import boot.data.dto.FaqDto;
import boot.data.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 1. REST API 컨트롤러임을 나타냅니다.
@RequestMapping("/api/faqs") // 2. 이 컨트롤러의 기본 URL 경로를 지정합니다.
@CrossOrigin // 3. 다른 도메인에서의 요청을 허용합니다 (CORS).
public class FaqController {

    @Autowired // 4. FaqService 객체를 자동으로 주입받습니다.
    private FaqService faqService;

    // --- 전체 FAQ 조회 (Read) ---
    @GetMapping
    public ResponseEntity<List<FaqDto>> getAllFaqs() {
        System.out.println("LOG: FaqController -> getAllFaqs() 호출됨");
        List<FaqDto> faqs = faqService.getAllFaqs(); // 서비스 호출
        return ResponseEntity.ok(faqs); // 조회된 FAQ 목록을 응답
    }

    // --- 새로운 FAQ 생성 (Create) ---
    @PostMapping
    public ResponseEntity<FaqDto> createFaq(@RequestBody FaqCreateRequest faqCreateRequest) {
        // ... (로그 출력)
        FaqDto createdFaq = faqService.createFaq(faqCreateRequest); // 서비스 호출
        return ResponseEntity.ok(createdFaq); // 생성된 FAQ 정보를 응답
    }

    // --- 기존 FAQ 수정 (Update) ---
    @PutMapping("/{id}")
    public ResponseEntity<FaqDto> updateFaq(@PathVariable Long id, @RequestBody FaqDto faqDto) {
        FaqDto updatedFaq = faqService.updateFaq(id, faqDto); // 서비스 호출
        if (updatedFaq != null) {
            return ResponseEntity.ok(updatedFaq); // 수정된 FAQ 정보를 응답
        } else {
            return ResponseEntity.notFound().build(); // 해당 id의 FAQ가 없으면 404 에러 응답
        }
    }

    // --- FAQ 삭제 (Delete) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id); // 서비스 호출
        return ResponseEntity.ok().build(); // 성공적으로 삭제되었음을 응답
    }
}