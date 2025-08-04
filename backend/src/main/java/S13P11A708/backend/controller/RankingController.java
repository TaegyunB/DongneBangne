package S13P11A708.backend.controller;

import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterChallengeListResponseDto;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterRankingDto;
import S13P11A708.backend.service.ChallengeService;
import S13P11A708.backend.service.RankingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/rankings")
public class RankingController {

    private final ChallengeService challengeService;
    private final RankingService rankingService;

    /**
     * 전체 경로당 랭킹 조회
     */
    @GetMapping
    public ResponseEntity<List<SeniorCenterRankingDto>> getAllRankings(){
        List<SeniorCenterRankingDto> rankings = rankingService.getAllRankings();
        return ResponseEntity.ok(rankings);
    }

    /**
     *  특정 경로당의 특정 년월 도전 목록 조회
     */
    @GetMapping("/senior-center/{seniorCenterId}/challenges")
    public ResponseEntity<SeniorCenterChallengeListResponseDto> getSeniorCenterChallengesByYearMonth(
            @PathVariable("seniorCenterId") Long seniorCenterId) {

        LocalDateTime now = LocalDateTime.now();
        Integer year = now.getYear();
        Integer month = now.getMonthValue();

        SeniorCenterChallengeListResponseDto response = challengeService.getSeniorCenterChallengesByYearMonth(seniorCenterId, year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 도전 상세 조회
     */
    @GetMapping("/senior-center/{seniorCenterId}/challenges/{challengeId}")
    public ResponseEntity<ChallengeResponseDto> getSeniorCenterChallengeDetail(
            @PathVariable("seniorCenterId") Long seniorCenterId,
            @PathVariable("challengeId") Long challengeId) {

        ChallengeResponseDto response = challengeService.getSeniorCenterChallengeDetail(challengeId, seniorCenterId);
        return ResponseEntity.ok(response);
    }
}
