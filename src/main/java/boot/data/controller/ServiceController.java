package boot.data.controller;

import boot.data.dto.FaqDto;
import boot.data.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class ServiceController {

    private final FaqService faqService;

    @GetMapping("/faqs")
    public ResponseEntity<List<FaqDto>> getAllFaqs() {
        List<FaqDto> faqs = faqService.getAllFaqs();
        return ResponseEntity.ok(faqs);
    }
}