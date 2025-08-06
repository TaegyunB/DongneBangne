package S13P11A708.backend.dto.request.challenge;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UploadChallengeImageAndDescriptionRequestDto {

    @NotNull(message = "이미지 파일은 필수입니다.")
    private MultipartFile imageFile;

    @NotNull(message = "이미지 설명은 필수입니다.")
    private String imageDescription;
}
