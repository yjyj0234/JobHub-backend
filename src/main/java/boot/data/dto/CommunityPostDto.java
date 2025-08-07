package boot.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostDto {

    // 게시글 정보
    private Long id;
    private String title;
    private String content;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 작성자 정보
    private Long userId;
    private String userNickname;
    private String userProfileImage; // 필요 시

    // 댓글 목록
    private List<CommentDto> comments;

    // 내부 댓글 DTO 정의
    @Getter
    @Setter
    public static class CommentDto {
        private Long id;
        private Long parentId; // 대댓글일 경우 부모 댓글 ID
        private String content;
        private boolean isDeleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // 댓글 작성자 정보
        private Long userId;
        private String userNickname;
        private String userProfileImage; // 필요 시
    }
}
