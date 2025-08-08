package S13P11A708.backend.repository;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.enums.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 전체 게시글 조회 (최신순)
     */
    @Query("SELECT b FROM Board b ORDER BY b.createdAt DESC ")
    List<Board> findAllOrderByCreatedAtDesc();

    /**
     * 카테고리별 전체 게시글 조회 (최신순)
     */
    @Query("SELECT b FROM Board b WHERE b.category = :category ORDER BY b.createdAt DESC")
    List<Board> findByCategoryOrderByCreatedAtDesc(BoardCategory category);

    /**
     * 인기글 조회 (좋아요 30개 이상)
     */
    @Query("SELECT b FROM Board b WHERE b.likeCount >= :likeCount ORDER BY b.createdAt DESC")
    List<Board> findPopularBoardOrderByCreatedAtDesc(@Param("likeCount") Integer likeCount);


}
