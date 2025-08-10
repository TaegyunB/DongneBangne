package S13P11A708.backend.service;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.Comment;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.request.comment.CommentCreateRequestDto;
import S13P11A708.backend.dto.response.comment.CommentResponseDto;
import S13P11A708.backend.repository.BoardRepository;
import S13P11A708.backend.repository.CommentRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    /**
     * 댓글 작성
     */
    public CommentResponseDto createComment(Long userId, Long boardId, CommentCreateRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .user(user)
                .board(board)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponseDto.from(savedComment);

    }

    /**
     * 게시글의 모든 댓글 조회
     */
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId) {

        if (!boardRepository.existsById(boardId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        List<Comment> allComments = commentRepository.findByBoardId(boardId);

        List<CommentResponseDto> result = allComments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 게시글의 댓글 수 조회
     */
    public Long getCommentCount(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        return commentRepository.countByBoardId(boardId);
    }

    /**
     * 댓글 수정
     */
    public CommentResponseDto updateComment(Long userId, Long commentId, CommentCreateRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->  new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }

        comment.updateContent(requestDto.getContent());
        Comment updatedComment = commentRepository.save(comment);

        return CommentResponseDto.from(updatedComment);
    }

    /**
     * 댓글 삭제
     */
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }









}
