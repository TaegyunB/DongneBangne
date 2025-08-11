package S13P11A708.backend.dto.response.comment;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.Comment;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.response.board.BoardDetailResponseDto;
import S13P11A708.backend.dto.response.user.UserProfileResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long commentId;
    private String content;
    private UserProfileResponseDto user;

    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .user(UserProfileResponseDto.from(comment.getUser()))
                .build();
    }

}
