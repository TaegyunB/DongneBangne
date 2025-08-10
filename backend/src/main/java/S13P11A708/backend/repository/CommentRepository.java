package S13P11A708.backend.repository;

import S13P11A708.backend.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 모든 댓글 조회 (최신순)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.board.id = :boardId ORDER BY c.createdAt ASC")
    List<Comment> findByBoardId(@Param("boardId") Long boardId);

    /**
     * 특정 게시글이 댓글 수 카운트
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.board.id = :boardId")
    Long countByBoardId(@Param("boardId") Long boardId);
}
