package S13P11A708.backend.dto.response.boardLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardLikeResponseDto {

    private Long boardId;
    private boolean isLiked;
    private Integer likeCount;
    private String message;


    public static BoardLikeResponseDto of(Long boardId, boolean isLiked, Integer likeCount) {
        return BoardLikeResponseDto.builder()
                .boardId(boardId)
                .isLiked(isLiked)
                .likeCount(likeCount)
                .build();
    }
}
