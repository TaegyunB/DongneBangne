package S13P11A708.backend.dto.response.aiNews;

import S13P11A708.backend.domain.AiNews;
import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiNewsResponseDto {

    private Long id;
    private String newsTitle;
    private String newsContent;
    private Integer year;
    private Integer month;
    private String pdfUrl;
    private String seniorCenterName;
    private List<ChallengeResponseDto> challenges;
    private Long successChallengeCount;
    private LocalDateTime createdAt;

    public static AiNewsResponseDto from(AiNews aiNews) {

        List<ChallengeResponseDto> challengeResponseDtos = Collections.emptyList();

        if (aiNews.getChallenges() != null) {
            challengeResponseDtos = aiNews.getChallenges().stream()
                    .map(ChallengeResponseDto::from)
                    .collect(Collectors.toList());
        }

        return AiNewsResponseDto.builder()
                .id(aiNews.getId())
                .newsTitle(aiNews.getNewsTitle())
                .year(aiNews.getYear())
                .month(aiNews.getMonth())
                .pdfUrl(aiNews.getPdfUrl())
                .seniorCenterName(aiNews.getSeniorCenter().getCenterName())
                .challenges(challengeResponseDtos)
                .successChallengeCount((long) challengeResponseDtos.size())
                .createdAt(aiNews.getCreatedAt())
                .build();
    }
}
