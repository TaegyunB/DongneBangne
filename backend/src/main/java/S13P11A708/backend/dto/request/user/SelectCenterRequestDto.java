package S13P11A708.backend.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SelectCenterRequestDto {
    private Long userId;
    private Long seniorCenterId;
}
