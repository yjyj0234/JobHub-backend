package boot.data.controller;

import boot.data.dto.FaqDto;
import boot.data.dto.FaqCreateRequest;
import boot.data.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/service")
@CrossOrigin
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private FaqService faqService;

    @GetMapping("/faqs")
    public List<FaqDto> getAllFaqs() {
        return faqService.getAllFaqs();
    }

    @PostMapping("/faqs")
    public ResponseEntity<?> createFaq(@RequestBody FaqCreateRequest createRequest) {
        try {
            FaqDto createdFaq = faqService.createFaq(createRequest);
            return new ResponseEntity<>(createdFaq, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating FAQ: ", e);
            return new ResponseEntity<>("FAQ 생성 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable Long id, @RequestBody FaqDto faqDto) {
        try {
            FaqDto updatedFaq = faqService.updateFaq(id, faqDto);
            return ResponseEntity.ok(updatedFaq);
        } catch (Exception e) {
            logger.error("Error updating FAQ with id {}: ", id, e);
            return new ResponseEntity<>("FAQ 수정 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}