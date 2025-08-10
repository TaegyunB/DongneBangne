package S13P11A708.backend.controller;

import S13P11A708.backend.dto.request.comment.CommentCreateRequestDto;
import S13P11A708.backend.dto.response.comment.CommentResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal CustomOAuth2User customUser,
            @Valid @RequestBody CommentCreateRequestDto requestDto) {

        Long userId = customUser.getUserId();
        CommentResponseDto response = commentService.createComment(userId, boardId, requestDto);

        return ResponseEntity.ok(response);
    }

    /**
     * 게시글의 모든 댓글 조회
     */
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable("boardId") Long boardId) {

        List<CommentResponseDto> comments = commentService.getCommentsByBoardId(boardId);

        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCommentCount(
            @PathVariable("boardId") Long boardId) {

        Long count = commentService.getCommentCount(boardId);
        return ResponseEntity.ok(count);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable("boardId") Long boardId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomOAuth2User customeUser,
            @Valid @RequestBody CommentCreateRequestDto requestDto) {

        Long userId = customeUser.getUserId();
        CommentResponseDto response = commentService.updateComment(userId, commentId, requestDto);

        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable("boardId") Long boardId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();
        commentService.deleteComment(userId, commentId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 삭제되었습니다.");
        response.put("commentId", commentId.toString());

        return ResponseEntity.ok(response);
    }


}
