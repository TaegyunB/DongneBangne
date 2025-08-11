package S13P11A708.backend.controller;

import S13P11A708.backend.domain.enums.ChallengeType;
import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import S13P11A708.backend.dto.response.challenge.MonthlyChallengeResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.ChallengeService;
import S13P11A708.backend.service.MonthlyChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
@Slf4j
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MonthlyChallengeService monthlyChallengeService;

    /**
     * 회원 경로당의 특정 년월 미션 조회
     */
    @GetMapping
    public ResponseEntity<MonthlyChallengeResponseDto> getMonthlyChallenges(
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();

        LocalDateTime now = LocalDateTime.now();
        Integer year = now.getYear();
        Integer month = now.getMonthValue();

        List<ChallengeResponseDto> allChallenges = challengeService.getMonthlyChallenges(userId, year, month);

        // 타입별로 구분
        List<ChallengeResponseDto> serviceChallenges = allChallenges.stream()
                .filter(c -> c.getChallengeType() == ChallengeType.SERVICE)
                .collect(Collectors.toList());

        List<ChallengeResponseDto> customChallenges = allChallenges.stream()
                .filter(c -> c.getChallengeType() == ChallengeType.CUSTOM)
                .collect(Collectors.toList());

        MonthlyChallengeResponseDto response = MonthlyChallengeResponseDto.builder()
                .year(year)
                .month(month)
                .serviceChallenges(serviceChallenges)
                .customChallenges(customChallenges)
                .totalCount(allChallenges.size())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 특정 도전 상세 조회
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeResponseDto> getChallengeDetail(
            @PathVariable("challengeId") Long challengeId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long userId = customUser.getUserId();

        ChallengeResponseDto response = challengeService.getChallengeDetail(challengeId, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 수동으로 현재 월 서비스 제공 미션 제공
     */
    @PostMapping("/create-service-challenges")
    public ResponseEntity<String> createServiceChallengesManually() {
        try {
            LocalDateTime now = LocalDateTime.now();
            Integer currentMonth = now.getMonthValue();
            Integer currentYear = now.getYear();

            monthlyChallengeService.executeServiceChallengeCreation(currentYear, currentMonth);

            return ResponseEntity.ok("서비스 제공 미션 생성 완료: " + currentYear + "년 " + currentMonth + "월");
        } catch (Exception e) {
            log.error("수동 서비스 미션 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("미션 생성 실패: " + e.getMessage());
        }
    }
}
