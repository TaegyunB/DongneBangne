package S13P11A708.backend.dto.request.board;

import S13P11A708.backend.domain.enums.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBoardRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private BoardCategory boardCategory;
}
