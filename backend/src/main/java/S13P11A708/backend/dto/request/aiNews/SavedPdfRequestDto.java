package S13P11A708.backend.dto.request.aiNews;

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
public class SavedPdfRequestDto {

    @NotNull
    private Long newsId;

    @NotBlank
    private String pdfUrl;
}