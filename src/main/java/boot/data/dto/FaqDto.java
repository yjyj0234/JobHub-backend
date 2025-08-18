package boot.data.dto;

import boot.data.entity.Faqs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqDto {
    private Long id;
    private String category;
    private String question;
    private String answer;

    public static FaqDto toDto(Faqs faq) {
        return FaqDto.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .build();
    }
}