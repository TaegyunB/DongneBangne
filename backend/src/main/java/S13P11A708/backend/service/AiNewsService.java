package S13P11A708.backend.service;

import S13P11A708.backend.domain.AiNews;
import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.response.aiNews.AiNewsResponseDto;
import S13P11A708.backend.dto.response.aiNews.GeneratePdfResponseDto;
import S13P11A708.backend.repository.AiNewsRepository;
import S13P11A708.backend.repository.ChallengeRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pqc.crypto.ExchangePair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiNewsService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final AiNewsRepository aiNewsRepository;
    private final OpenAiService openAiService;
    private final PdfGenerationService pdfGenerationService;
    private final S3Service s3Service;

    /**
     * AI 신문 생성 (비동기 처리)
     */
    @Async
    public CompletableFuture<AiNewsResponseDto> createAiNews(Long userId) {

        try {
            SeniorCenter seniorCenter = validateAndGetSeniorCenter(userId);

            // 현재 년월 자동 설정
            LocalDateTime now = LocalDateTime.now();
            Integer currentYear = now.getYear();
            Integer currentMonth = now.getMonthValue();

            List<Challenge> completedChallenges = challengeRepository.findCompletedChallengesByYearAndMonth(
                    seniorCenter.getId(),
                    currentYear,
                    currentMonth);

            if (completedChallenges.isEmpty()) {
                throw new IllegalArgumentException("해당 년월에 완료된 미션이 없습니다.");
            }

            // OpenAI API로 신문 제목 생성
            String newsTitle = openAiService.generateNewsTitle(
                    seniorCenter.getCenterName(), currentYear, currentMonth);

            // 각 챌린지별로 AI 기사 생성
            for (Challenge challenge : completedChallenges) {
                if (challenge.getImageDescription() != null && !challenge.getImageDescription().isEmpty()) {
                    String aiArticle = openAiService.generateChallengeArticle(challenge);
                    challenge.updateAiDescription(aiArticle);

                    // API 호출 간격 (Rate Limiting 방지)
                    Thread.sleep(500);
                }
            }

            // AI News 엔티티 생성
            AiNews aiNews = AiNews.builder()
                    .seniorCenter(seniorCenter)
                    .year(currentYear)
                    .month(currentMonth)
                    .newsTitle(newsTitle)
                    .build();

            AiNews savedAiNews = aiNewsRepository.save(aiNews);
            log.info("AI 신문 콘텐츠 생성 완료: newsId={}", savedAiNews.getId());

            // Challenge와 연관관계 설정
            completedChallenges.forEach(challenge -> {
                challenge.setAiNews(savedAiNews);
                savedAiNews.getChallenges().add(challenge);
            });

            challengeRepository.saveAll(completedChallenges);

            AiNews completedAiNews = aiNewsRepository.save(savedAiNews);
            log.info("AI 신문 생성 완료: newsId={}", completedAiNews.getId());

            return CompletableFuture.completedFuture(AiNewsResponseDto.from(completedAiNews));

        } catch(Exception e) {
            log.error("AI 신문 생성 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("AI 신문 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * PDF 생성
     */
    public GeneratePdfResponseDto generatePdf(Long newsId, Long userId, String newsPaper) {
        try {
            SeniorCenter seniorCenter = validateAndGetSeniorCenter(userId);

            // AI 신문 조회 및 권한 확인
            AiNews aiNews = aiNewsRepository.findById(newsId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 AI 신문입니다."));

            LocalDateTime now = LocalDateTime.now();
            Integer year = now.getYear();
            Integer month = now.getMonthValue();

            if (!aiNews.getSeniorCenter().getId().equals(seniorCenter.getId())) {
                throw new IllegalArgumentException("해당 AI 신문에 접근할 권한이 없습니다.");
            }

            // 이미 PDF가 생성되어 있으면 기존 URL 반환
            if (aiNews.getPdfUrl() != null && !aiNews.getPdfUrl().isEmpty()) {
                return GeneratePdfResponseDto.from(aiNews);
            }

            // 프론트에서 받은 HTML로 PDF 생성
            byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml(newsPaper);

            // S3에 PDF 업로드
            String fileName = String.format("ai-news/%s_%d_%d_%d.pdf",
                    seniorCenter.getCenterName(), year, month, aiNews.getId());

            String pdfUrl = s3Service.uploadByteDataFile(fileName, pdfBytes, "application/pdf");

            // AiNews에 PDF URL 저장
            aiNews.updateContent(pdfUrl);
            AiNews updatedAiNews = aiNewsRepository.save(aiNews);

            return GeneratePdfResponseDto.from(updatedAiNews);

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    /**
     * AI 신문 목록 조회
     */
    public List<AiNewsResponseDto> getAiNewsList(Long userId) {
        SeniorCenter seniorCenter = validateAndGetSeniorCenter(userId);

        List<AiNews> newsList = aiNewsRepository.findAiNewsBySeniorCenterIdOrderByCreatedAtDesc(seniorCenter.getId());

        return newsList.stream()
                .map(AiNewsResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * AI 신문 상세 조회
     */
    public AiNewsResponseDto getAiNewsDetail(Long newsId, Long userId) {
        SeniorCenter seniorCenter = validateAndGetSeniorCenter(userId);

        AiNews aiNews = aiNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 AI 신문입니다."));

        // 권한 확인 (해당 경로당의 신분인지)
        if (!aiNews.getSeniorCenter().getId().equals(seniorCenter.getId())) {
            throw new IllegalArgumentException("해당 AI 신문에 접근할 권한이 없습니다.");
        }

        return AiNewsResponseDto.from(aiNews);
    }

    //== 공통 검증 메서드 ==//
    /**
     * 관리자 권한 및 경로당 소속 검증
     */
    private SeniorCenter validateAndGetSeniorCenter(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        return seniorCenter;
    }
}
