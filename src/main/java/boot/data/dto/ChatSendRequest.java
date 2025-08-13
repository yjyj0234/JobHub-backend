package boot.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter 
@Setter
public class ChatSendRequest { //메세지 요청과 응답 분리 dto
    @NotNull
    private Long userId;

    @NotBlank
    private String roomKey;

    @NotBlank
    private String message;
}
