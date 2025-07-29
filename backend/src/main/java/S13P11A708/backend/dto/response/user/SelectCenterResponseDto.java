package S13P11A708.backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class SelectCenterResponseDto {
    private boolean success;
    private String message;
}
