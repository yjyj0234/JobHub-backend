package boot.data.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityCommentDto {
    
    private Long id; // 댓글 ID
    private Long postId; // 게시글 ID
    private Long userId; // 작성자 ID
    private String content; // 댓글 내용
    private boolean isDeleted; // 삭제 여부

    private String userName; // 작성자 이름

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 작성일

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt; // 수정일



    private boolean isOwner; // 작성자 여부
    private boolean isAdmin; // 관리자 여부
}
