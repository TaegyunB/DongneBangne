package S13P11A708.backend.controller;

import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challegeService;

    /**
     * 회원 경로당의 특정 년월 미션 조회
     */
    @GetMapping
    public ResponseEntity<List<ChallengeResponseDto>> getMonthlyChallenges(
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();

        LocalDateTime now = LocalDateTime.now();
        Integer year = now.getYear();
        Integer month = now.getMonthValue();

        List<ChallengeResponseDto> challenges = challegeService.getMonthlyChallenges(userId, year, month);

        return ResponseEntity.status(HttpStatus.OK).body(challenges);
    }

    /**
     * 특정 도전 상세 조회
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeResponseDto> getChallengeDetail(
            @PathVariable("challengeId") Long challengeId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();

        ChallengeResponseDto response = challegeService.getChallengeDetail(challengeId, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
