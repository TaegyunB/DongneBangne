package S13P11A708.backend.controller;

import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam Integer year,
            @RequestParam Integer month,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();
        List<ChallengeResponseDto> challenges = challegeService.getMonthlyChallenges(userId, year, month);

        return ResponseEntity.status(HttpStatus.OK).body(challenges);
    }

}
