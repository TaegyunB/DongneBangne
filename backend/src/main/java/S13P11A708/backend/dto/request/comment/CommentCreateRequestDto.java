package S13P11A708.backend.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDto {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
