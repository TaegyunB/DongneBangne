package S13P11A708.backend.dto.response.board;

import S13P11A708.backend.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDetailResponseDto {

    private Long boardId;
    private String nickname;
    private String seniorCenterName;
    private String title;
    private String content;
    private String category;
    private String boardImage;
    private Integer likeCount;
    private LocalDateTime createdAt;

    public static BoardDetailResponseDto from(Board board) {
        return BoardDetailResponseDto.builder()
                .boardId(board.getId())
                .nickname(board.getUser().getNickname())
                .seniorCenterName(board.getUser().getSeniorCenter().getCenterName())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getCategory().name())
                .boardImage(board.getBoardImage())
                .likeCount(board.getLikeCount())
                .createdAt(board.getCreatedAt())
                .build();
    }

}
