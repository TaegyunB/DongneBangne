package S13P11A708.backend.dto.response.aiNews;

import S13P11A708.backend.domain.AiNews;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePdfResponseDto {

    private Long aiNewsId;
    private String pdfUrl;
    private LocalDateTime createdAt;
    private SeniorCenterDetailResponseDto seniorCenter;

    public static GeneratePdfResponseDto from(AiNews aiNews) {
        return GeneratePdfResponseDto.builder()
                .aiNewsId(aiNews.getId())
                .pdfUrl(aiNews.getPdfUrl())
                .createdAt(aiNews.getCreatedAt())
                .seniorCenter(SeniorCenterDetailResponseDto.from(aiNews.getSeniorCenter()))
                .build();
    }
}
