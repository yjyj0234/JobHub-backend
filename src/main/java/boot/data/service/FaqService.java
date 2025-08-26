package boot.data.service;

import boot.data.dto.FaqCreateRequest;
import boot.data.dto.FaqDto;
import boot.data.entity.Faqs;
import boot.data.repository.FaqRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<FaqDto> getAllFaqs() {
        return faqRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FaqDto createFaq(FaqCreateRequest request) {
        Faqs faq = new Faqs();
        faq.setCategory(request.getCategory());
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());

        Faqs savedFaq = faqRepository.saveAndFlush(faq);
        return convertToDto(savedFaq);
    }

    @Transactional
    public FaqDto updateFaq(Long id, FaqDto faqDto) {
        Faqs faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found with id: " + id));

        faq.setCategory(faqDto.getCategory());
        faq.setQuestion(faqDto.getQuestion());
        faq.setAnswer(faqDto.getAnswer());

        Faqs updatedFaq = faqRepository.saveAndFlush(faq);
        return convertToDto(updatedFaq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new EntityNotFoundException("FAQ not found with id: " + id);
        }
        faqRepository.deleteById(id);
    }

    private FaqDto convertToDto(Faqs faq) {
        return FaqDto.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}