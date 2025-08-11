package S13P11A708.backend.controller;

import S13P11A708.backend.dto.response.aiNews.AiNewsResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.AiNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-news")
@RequiredArgsConstructor
@Slf4j
public class AiNewsController {

    private final AiNewsService aiNewsService;

    /**
     * AI 신문 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<AiNewsResponseDto>> getAiNewsList(
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();
        List<AiNewsResponseDto> newsList = aiNewsService.getAiNewsList(userId);

        log.info("AI 신문 목록 조회: userId={}, count={}", userId, newsList.size());
        return ResponseEntity.ok(newsList);
    }

    /**
     * 특정 AI 신문 상세 조회
     */
    @GetMapping("/{aiNewsId}")
    public ResponseEntity<AiNewsResponseDto> getAiNewsDetail(
            @PathVariable("aiNewsId") Long aiNewsId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();
        AiNewsResponseDto newsDetail = aiNewsService.getAiNewsDetail(aiNewsId, userId);

        return ResponseEntity.ok(newsDetail);
    }

}

