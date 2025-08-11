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
public class GeneratePdfUrlResponseDto {

    private Long aiNewsId;
    private String pdfUrl;
    private Boolean isGenerated;
    private LocalDateTime createdAt;
    private SeniorCenterDetailResponseDto seniorCenter;

    public static GeneratePdfUrlResponseDto from(AiNews aiNews) {
        return GeneratePdfUrlResponseDto.builder()
                .aiNewsId(aiNews.getId())
                .pdfUrl(aiNews.getPdfUrl())
                .isGenerated(aiNews.isGenerated())
                .createdAt(aiNews.getCreatedAt())
                .seniorCenter(SeniorCenterDetailResponseDto.from(aiNews.getSeniorCenter()))
                .build();
    }

    /**
     * 에러 상태 응답용
     */
    public static GeneratePdfUrlResponseDto error(Long aiNewsId) {
        return GeneratePdfUrlResponseDto.builder()
                .aiNewsId(aiNewsId)
                .pdfUrl(null)
                .isGenerated(false)
                .createdAt(null)
                .seniorCenter(null)
                .build();
    }


}
