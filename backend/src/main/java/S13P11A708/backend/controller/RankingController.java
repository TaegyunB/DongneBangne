package S13P11A708.backend.controller;

import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterRankingDto;
import S13P11A708.backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<SeniorCenterRankingDto>> getAllRankings(){
        List<SeniorCenterRankingDto> rankings = rankingService.getAllRankings();
        return ResponseEntity.ok(rankings);

    }
}
