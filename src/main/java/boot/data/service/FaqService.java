package boot.data.service;

import boot.data.dto.FaqDto;
import boot.data.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public List<FaqDto> getAllFaqs() {
        return faqRepository.findAllByOrderByIdAsc().stream()
                .map(FaqDto::toDto)
                .collect(Collectors.toList());
    }
}