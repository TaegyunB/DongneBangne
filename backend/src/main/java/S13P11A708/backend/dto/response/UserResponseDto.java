package S13P11A708.backend.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDto {
    private Long userId;
    private Long seniorCenterId;
    private Long userName;
    private String profileImage;
//    private Enum
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
