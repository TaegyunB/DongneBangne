package S13P11A708.backend.service;

import S13P11A708.backend.domain.AiNews;
import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.dto.request.aiNews.SavedPdfRequestDto;
import S13P11A708.backend.dto.response.aiNews.AiNewsResponseDto;
import S13P11A708.backend.dto.response.aiNews.GeneratePdfUrlResponseDto;
import S13P11A708.backend.dto.response.challenge.CompleteChallengeResponseDto;
import S13P11A708.backend.repository.AiNewsRepository;
import S13P11A708.backend.repository.ChallengeRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.AIMDBackoffManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final S3Service s3Service;

    /**
     * AI 신문 목록 조회
     */
    public List<AiNewsResponseDto> getAiNewsList(Long userId) {
        SeniorCenter seniorCenter = validateMemberAndGetSeniorCenter(userId);

        List<AiNews> newsList = aiNewsRepository.findBySeniorCenterIdOrderByCreatedAtDesc(seniorCenter.getId());

        return newsList.stream()
                .map(AiNewsResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * AI 신문 상세 조회
     */
    public AiNewsResponseDto getAiNewsDetail(Long newsId, Long userId) {
        SeniorCenter seniorCenter = validateMemberAndGetSeniorCenter(userId);

        AiNews aiNews = aiNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 AI 신문입니다."));

        // 권한 확인 (해당 경로당의 신분인지)
        if (!aiNews.getSeniorCenter().getId().equals(seniorCenter.getId())) {
            throw new IllegalArgumentException("해당 AI 신문에 접근할 권한이 없습니다.");
        }

        return AiNewsResponseDto.from(aiNews);
    }

    /**
     * AI 신문 PDF URL 조회
     */
    public String getNewsPdfUrl(Long userId, Long newsId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        AiNews aiNews = aiNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 AI 신문을 찾을 수 없습니다."));


        // 같은 센터 소속인지 확인
        if (!aiNews.getSeniorCenter().getId().equals(user.getSeniorCenter().getId())) {
            throw new IllegalArgumentException("해당 AI 신문에 대한 권한이 없습니다.");
        }

        if (aiNews.getPdfUrl() == null || aiNews.getPdfUrl().isEmpty()) {
            throw new IllegalArgumentException("생성된 PDF가 없습니다.");
        }

        return aiNews.getPdfUrl();
    }

    //== Admin 전용 메서드 ==//
    /**
     * AI 신문 생성 - 비동기 처리 (Admin 전용)
     */
    @Async
    public CompletableFuture<AiNewsResponseDto> createAiNews(Long userId) {

        try {
            SeniorCenter seniorCenter = validateAdminAndGetSeniorCenter(userId);

            // 현재 년월 자동 설정
            LocalDateTime now = LocalDateTime.now();
            Integer currentYear = now.getYear();
            Integer currentMonth = now.getMonthValue();

            // 이미 생성된 AI 신문이 있는지 확인
            Optional<AiNews> existingNews = aiNewsRepository.findAiNewsBySeniorCenterIdAndYearAndMonth(
                    seniorCenter.getId(), currentYear, currentMonth);

            if (existingNews.isPresent()) {
                AiNews existing = existingNews.get();
                return CompletableFuture.completedFuture(AiNewsResponseDto.from(existing));
            }

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
                    .pdfUrl(null)
                    .isGenerated(false)
                    .build();

            AiNews savedAiNews = aiNewsRepository.save(aiNews);

            // Challenge와 연관관계 설정
            completedChallenges.forEach(challenge -> {
                challenge.setAiNews(savedAiNews);
                savedAiNews.getChallenges().add(challenge);
            });

            challengeRepository.saveAll(completedChallenges);

            AiNews completedAiNews = aiNewsRepository.save(savedAiNews);

            return CompletableFuture.completedFuture(AiNewsResponseDto.from(completedAiNews));

        } catch(Exception e) {
            throw new RuntimeException("AI 신문 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * AI 신문 PDF 업로드 및 저장
     */
    public String uploadNewsPdf(Long userId, Long newsId, MultipartFile pdfFile) {

        SeniorCenter seniorCenter = validateAdminAndGetSeniorCenter(userId);

        AiNews aiNews = aiNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 AI 신문을 찾을 수 없습니다."));

        if (!aiNews.getSeniorCenter().getId().equals(seniorCenter.getId())) {
            throw new IllegalArgumentException("해당 AI 신문에 대한 권한이 없습니다.");
        }

        try {
            // S3에 PDF 업로드
            String fileName = String.format("ai-news/%s/%d/%d_%d.pdf",
                    seniorCenter.getCenterName(),
                    seniorCenter.getId(),
                    aiNews.getYear(),
                    aiNews.getMonth());

            String pdfUrl = s3Service.uploadMultipartFile(pdfFile, fileName);

            // AI 신문 엔티티에 PDF URL 저장
            aiNews.updatePdfUrl(pdfUrl);
            aiNews.updateGenerated(true);

            aiNewsRepository.save(aiNews);

            return pdfUrl;

        } catch (Exception e) {
            throw new RuntimeException("PDF 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //== 공통 검증 메서드 ==//
    /**
     * 회원 권한 검증
     */
    private SeniorCenter validateMemberAndGetSeniorCenter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = user.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        return seniorCenter;
    }

    /**
     * 관리자 권한 및 경로당 소속 검증
     */
    private SeniorCenter validateAdminAndGetSeniorCenter(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        if (admin.getUserRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 관리자입니다.");
        }

        if (seniorCenter.getAdminUserId() == null) {
            throw new IllegalArgumentException("해당 경로당에 관리자가 설정되지 않았습니다.");
        }

        if (!seniorCenter.getAdminUserId().equals(adminId)) {
            throw new IllegalArgumentException("해당 경로당의 관리자가 아닙니다");
        }

        return seniorCenter;
    }
}