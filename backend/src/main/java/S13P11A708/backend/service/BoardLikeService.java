package S13P11A708.backend.service;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.BoardLike;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.response.boardLike.BoardLikeResponseDto;
import S13P11A708.backend.repository.BoardLikeRepository;
import S13P11A708.backend.repository.BoardRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardLikeService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;

    /**
     * 게시글 좋아요 토글 (좋아요/좋아요 취소)
     */
    public BoardLikeResponseDto toggleBoardLike(Long userId, Long boardId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        boolean isLiked = boardLikeRepository.existsByUserAndBoard(user, board);

        if (isLiked) {
            boardLikeRepository.deleteByUserIdAndBoardId(userId, boardId);
            board.decreaseLike();
        } else {
            BoardLike boardLike = BoardLike.builder()
                    .user(user)
                    .board(board)
                    .build();

            boardLikeRepository.save(boardLike);
            board.increaseLike();
        }

        Board savedBoard = boardRepository.save(board);

        return BoardLikeResponseDto.builder()
                .boardId(boardId)
                .isLiked(!isLiked)
                .likeCount(savedBoard.getLikeCount())
                .build();
    }


}
