package S13P11A708.backend.controller.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import S13P11A708.backend.dto.request.aiNews.SavedPdfRequestDto;
import S13P11A708.backend.dto.response.aiNews.AiNewsResponseDto;
import S13P11A708.backend.dto.response.aiNews.GeneratePdfUrlResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.AiNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/ai-news")
@RequiredArgsConstructor
@Slf4j
public class AiNewsAdminController {

    private final AiNewsService aiNewsService;

    /**
     * 완료된 미션 당 AI가 내용 정리
     */
    @PostMapping("/create")
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
     * PDF 업로드
     */
    @PostMapping("/upload-pdf")
    public ResponseEntity<Map<String, Object>> uploadNewsPdf(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("newsId") Long newsId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        try {
            Long userId = customUser.getUserId();
            String pdfUrl = aiNewsService.uploadNewsPdf(userId, newsId, pdfFile);

            Map<String, Object> response = new HashMap<>();
            response.put("pdfUrl", pdfUrl);
            response.put("message", "PDF 업로드 성공");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("PDF 업로드에 실패했습니다.", e);
        }
    }

    /**
     * PDF 조회 엔드포인트
     */
    @GetMapping("/{newsId}/pdf")
    public ResponseEntity<Map<String, Object>> getNewsPdf(
            @PathVariable Long newsId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        try {
            Long userId = customUser.getUserId();
            String pdfUrl = aiNewsService.getNewsPdfUrl(userId, newsId);

            Map<String, Object> response = new HashMap<>();
            response.put("pdfUrl", pdfUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("PDF를 찾을 수 없습니다.", e);
        }
    }
}
