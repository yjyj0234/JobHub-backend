package boot.data.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.CommunityCommentDto;
import boot.data.security.CurrentUser;
import boot.data.service.CommunityCommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Validated
public class CommunityCommentController {

    private final CommunityCommentService commentService;
    private final CurrentUser currentUser; // 로그인 사용자 id 헬퍼

    // 1) 특정 게시글의 댓글 목록
    // GET /community/{postId}/comments
    @GetMapping("/{postId}/comments")
    public List<CommunityCommentDto> listByPost(@PathVariable("postId") Long postId) {
        return commentService.getCommentsByPost(postId);
    }

    // 2) 댓글 작성
    // POST /community/{postId}/comments
    // body: { "content": "댓글 내용" }
    @PostMapping("/{postId}/comments")
    public CommunityCommentDto create(@PathVariable("postId") Long postId,
                                      @Valid @RequestBody CreateCommentRequest req) {
        // 비로그인 시 내부에서 예외
        return commentService.addComment(postId, req.getContent());
    }

    // 3) 댓글 삭제(Soft delete: 내용 비우기)
    // DELETE /community/comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId, Authentication authentication) {
        Long requesterUserId = currentUser.idOrThrow(); // 로그인 사용자
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        commentService.deleteComment(commentId, requesterUserId, isAdmin);
        // 반환값/어노테이션 없이 성공 시 기본 200 OK
    }

    // ====== helpers ======
    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (role.equals(auth.getAuthority())) return true;
        }
        return false;
    }

    // 요청 Body용 DTO
    @Getter @Setter
    public static class CreateCommentRequest {
        @NotBlank(message = "댓글 내용을 입력해줘")
        private String content;
    }
}
