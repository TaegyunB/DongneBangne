package S13P11A708.backend.controller.admin;

import S13P11A708.backend.dto.response.aiNews.AiNewsResponseDto;
import S13P11A708.backend.dto.response.aiNews.GeneratePdfResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.AiNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/admin/ai-news")
@RequiredArgsConstructor
@Slf4j
public class AiNewsAdminController {

    private final AiNewsService aiNewsService;

    /**
     * 완료된 미션 당 AI가 내용 정리
     */
    @PostMapping
    public ResponseEntity<AiNewsResponseDto> createAiNews(
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        try {
            Long userId = customUser.getUserId();

            // 비동기로 AI 신문 생성 시작
            CompletableFuture<AiNewsResponseDto> future = aiNewsService.createAiNews(userId);
            AiNewsResponseDto result = future.get(); // 결과를 기다림

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("AI 신문 생성 요청 검증 실패: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        } catch (Exception e) {
            log.error("AI 신문 생성 요청 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("AI 신문 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * AI 신문 PDF 생성
     */
    @PostMapping("/{aiNewsId}/generate-pdf")
    public ResponseEntity<GeneratePdfResponseDto> generateAiNewsPdf(
            @PathVariable("aiNewsId") Long aiNewsId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        try {
            Long userId = customUser.getUserId();
            String newsPaper = request.get("newsPaper");

            if (newsPaper == null || newsPaper.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // PDF 생성 및 URL 반환
            GeneratePdfResponseDto response = aiNewsService.generatePdf(aiNewsId, userId, newsPaper);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("AI 신문 PDF 다운로드 권한 오류: newsId={}, error={}", aiNewsId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            log.error("AI 신문 PDF 다운로드 중 오류: newsId={}", aiNewsId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
