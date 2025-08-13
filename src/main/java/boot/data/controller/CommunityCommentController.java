package boot.data.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.CommunityCommentDto;
import boot.data.service.CommunityCommentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/community/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityCommentService commentService;

    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<List<CommunityCommentDto>> list(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    // 댓글 등록
    @PostMapping
    public ResponseEntity<CommunityCommentDto> create(
            @PathVariable("postId") Long postId,
            @RequestBody CreateCommentRequest req
    ) {
        // 실제 서비스에선 userId는 토큰에서 꺼내는 게 안정적.
        CommunityCommentDto saved = commentService.addComment(postId, req.content());
        return ResponseEntity.ok(saved);
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommunityCommentDto> update(
            @PathVariable("postId") Long postId, // 필요 시 검증에 사용 가능
            @PathVariable("commentId") Long commentId,
            @RequestBody UpdateCommentRequest req
    ) {
        CommunityCommentDto updated = commentService.updateComment(commentId, req.content());
        return ResponseEntity.ok(updated);
    }

    // 댓글 삭제(soft)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId
            // @RequestBody DeleteCommentRequest req
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // ====== 요청 바디용 record DTO ======
    public record CreateCommentRequest(Long userId, String content) {}
    public record UpdateCommentRequest(Long editorUserId, String content) {}
    // public record DeleteCommentRequest(Long requesterUserId, boolean isAdmin) {}
}
