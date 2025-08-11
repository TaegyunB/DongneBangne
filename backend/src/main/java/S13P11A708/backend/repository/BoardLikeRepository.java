package S13P11A708.backend.repository;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.BoardLike;
import S13P11A708.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     */
    boolean existsByUserAndBoard(User user, Board board);

    /**
     * 특정 게시글의 좋아요 수 카운트
     */
    @Query("SELECT COUNT(bl) FROM BoardLike bl WHERE bl.board.id = :boardId")
    Long countByBoardId(@Param("boardId") Long boardId);

    /**
     * 사용자와 게시글 ID로 좋아요 삭제
     */
    void deleteByUserIdAndBoardId(Long userId, Long boardId);
}
