package S13P11A708.backend.dto.request.board;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.enums.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequestDto {

    @NotBlank(message = "게시글 제목은 필수입니다.")
    @Size(max = 500, message = "게시글 제목은 500자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "게시글 내용은 필수입니다.")
    @Size(max = 1000, message = "게시글 내용은 1000자를 초과할 수 없습니다.")
    private String content;

    @NotNull(message = "카테고리는 필수입니다.")
    private BoardCategory boardCategory;

    private String boardImage;

    public static BoardCreateRequestDto from(Board board) {
        return BoardCreateRequestDto.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .boardCategory(board.getCategory())
                .boardImage(board.getBoardImage())
                .build();
    }
}
