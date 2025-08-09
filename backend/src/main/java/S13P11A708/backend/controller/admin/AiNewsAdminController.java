package S13P11A708.backend.controller.admin;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import S13P11A708.backend.dto.request.aiNews.SavedPdfRequestDto;
import S13P11A708.backend.dto.response.aiNews.AiNewsResponseDto;
import S13P11A708.backend.dto.response.aiNews.GeneratePdfUrlResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.AiNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
     * 프론트에서 생성된 PDF URL을 받아서 S3에 저장
     */
    @PostMapping("/save-pdf")
    public ResponseEntity<GeneratePdfUrlResponseDto> savePdfFromUrl(
            @Valid @RequestBody SavedPdfRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        try {
            Long userId = customUser.getUserId();

            GeneratePdfUrlResponseDto response = aiNewsService.savePdfFromUrl(requestDto, userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            GeneratePdfUrlResponseDto errorResponse = GeneratePdfUrlResponseDto.error(requestDto.getNewsId());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {

            GeneratePdfUrlResponseDto errorResponse = GeneratePdfUrlResponseDto.error(
                    requestDto != null ? requestDto.getNewsId() : null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
