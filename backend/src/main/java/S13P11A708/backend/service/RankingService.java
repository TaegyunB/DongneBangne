package S13P11A708.backend.service;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterPointResponseDto;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterRankingDto;
import S13P11A708.backend.repository.SeniorCenterRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RankingService {

    private final UserRepository userRepository;
    private final SeniorCenterRepository seniorCenterRepository;

    /**
     * 전체 랭킹 조회
     */
    @Cacheable(value = "rankings", key = "'all'")
    public List<SeniorCenterRankingDto> getAllRankings() {
        List<SeniorCenter> seniorCenters = seniorCenterRepository.orderSeniorCenterByTotalPointDesc();

        return IntStream.range(0, seniorCenters.size())
                .mapToObj(i -> SeniorCenterRankingDto.from(seniorCenters.get(i), i+1))
                .toList();
    }

}
