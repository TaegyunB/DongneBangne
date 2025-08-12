package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import S13P11A708.backend.domain.enums.BoardCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private BoardCategory category;

    @Column(name = "board_image")
    private String boardImage;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //== Setter 메서드 ==//
    /**
     * 게시글 수정
     */
    public void updateBoard(String title, String content, BoardCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    /**
     * 이미지 업로드 setter
     */
    public void updateBoardImage(String boardImage) {
        this.boardImage = boardImage;
    }

    /**
     * 좋아요 수 증가 setter
     */
    public void increaseLike() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 감소 setter
     */
    public void decreaseLike() {
        this.likeCount--;
    }

}
