package S13P11A708.backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CenterStatusResponseDto {
    private boolean hasCenter;
    private String message;
}
