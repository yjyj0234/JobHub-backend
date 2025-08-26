// JobHub-backend/src/main/java/boot/data/service/FaqService.java

package boot.data.service;

import boot.data.dto.FaqCreateRequest;
import boot.data.dto.FaqDto;
import boot.data.entity.Faqs;
import boot.data.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    public List<FaqDto> getAllFaqs() {
        return faqRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public FaqDto createFaq(FaqCreateRequest faqCreateRequest) {
        // --- 강제 로그 추가 ---
        System.out.println("LOG: FaqService -> createFaq() 호출됨");
        // --------------------

        Faqs newFaqEntity = new Faqs();
        newFaqEntity.setCategory(faqCreateRequest.getCategory());
        newFaqEntity.setTitle(faqCreateRequest.getTitle());
        newFaqEntity.setContent(faqCreateRequest.getContent());

        System.out.println("LOG: DB에 저장할 Faqs 엔티티 객체 생성 완료.");
        System.out.println("LOG: 이제 repository.save()를 호출하여 DB에 저장합니다.");

        Faqs savedFaq = faqRepository.save(newFaqEntity);

        System.out.println("LOG: DB 저장 성공! 저장된 ID: " + savedFaq.getId());

        return convertToDto(savedFaq);
    }

    public FaqDto updateFaq(Long id, FaqDto faqDto) {
        return faqRepository.findById(id)
                .map(faq -> {
                    faq.setCategory(faqDto.getCategory());
                    faq.setTitle(faqDto.getTitle());
                    faq.setContent(faqDto.getContent());
                    return convertToDto(faqRepository.save(faq));
                })
                .orElse(null);
    }

    public void deleteFaq(Long id) {
        faqRepository.deleteById(id);
    }

    private FaqDto convertToDto(Faqs faq) {
        return FaqDto.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .title(faq.getTitle())
                .content(faq.getContent())
                .build();
    }
}