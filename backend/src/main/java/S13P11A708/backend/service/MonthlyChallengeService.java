package S13P11A708.backend.service;

import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.enums.ChallengeType;
import S13P11A708.backend.repository.ChallengeRepository;
import S13P11A708.backend.repository.SeniorCenterRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@BatchTransactionManager
@Slf4j
public class MonthlyChallengeService {

    private final ChallengeRepository challengeRepository;
    private final SeniorCenterRepository seniorCenterRepository;

    private static final int MAX_SERVICE_CHALLENGES_PER_MONTH = 2;


    // 월별 서비스 제공 미션 데이터
    private static final Map<Integer, List<ServiceChallenge>> SERVICE_CHALLENGES;

    static {
        Map<Integer, List<ServiceChallenge>> challenges = new HashMap<>();

        challenges.put(1, List.of(
                new ServiceChallenge("새해 덕담 나누기", "따뜻한 덕담과 새해 소망을 서로 나누기", "경로당"),
                new ServiceChallenge("떡국 먹으며 옛이야기", "떡국을 먹으며 어린 시절 이야기 나누기", "경로당")
        ));

        challenges.put(2, List.of(
                new ServiceChallenge("우리 경로당 사진첩 만들기", "지난 1년간 활동 사진을 모아 앨범 만들기", "경로당"),
                new ServiceChallenge("겨울 건강 운동", "실내에서 할 수 있는 간단한 체조와 스트레칭", "동네 마트")
        ));

        challenges.put(3, List.of(
                new ServiceChallenge("봄나들이 걷기", "근처 공원이나 산책로를 함께 걸으며 꽃 구경", "근처 산책로"),
                new ServiceChallenge("작은 선물 뽑기 이벤트", "선물 상자 속 행운을 뽑아보세요", "경로당 마당")
        ));

        challenges.put(4, List.of(
                new ServiceChallenge("전통놀이 한마당", "제기차기, 투호, 윷놀이로 팀별 대결", "경로당"),
                new ServiceChallenge("경로당 영화관", "큰 화면으로 추억의 영화나 드라마를 함께 보고 이야기 나누기", "경로당")
        ));

        challenges.put(5, List.of(
                new ServiceChallenge("아침 스트레칭", "10분 동안 음악에 맞춰 스트레칭! 사진 인증 필수", "공원"),
                new ServiceChallenge("어린 시절 놀이 체험", "딱지치기, 구슬치기, 고무줄놀이 함께 하기", "경로당")
        ));

        challenges.put(6, List.of(
                new ServiceChallenge("여름나기 비법 공유", "더위를 피하는 나만의 방법 이야기", "경로당"),
                new ServiceChallenge("시원한 음식 만들기", "냉국, 오이무침 등 여름 음식 함께 조리", "경로당 주방")
        ));

        challenges.put(7, List.of(
                new ServiceChallenge("부채 꾸미기", "부채에 그림이나 글귀를 적어 꾸미기", "경로당"),
                new ServiceChallenge("여름 노래방", "여름 노래나 신나는 곡으로 노래자랑", "경로당")
        ));

        challenges.put(8, List.of(
                new ServiceChallenge("물놀이 소풍", "근처 계곡이나 냇가에서 발 담그기", "인근 야외"),
                new ServiceChallenge("빙수 만들기", "과일과 팥을 넣은 빙수 만들어 나누기", "경로당 주방")
        ));

        challenges.put(9, List.of(
                new ServiceChallenge("송편 빚기 대회", "팀별로 예쁜 송편 만들고 시식 투표", "경로당 주방"),
                new ServiceChallenge("추석 노래·시 대회", "추석 주제로 노래 부르기나 시 낭송", "경로당")
        ));

        challenges.put(10, List.of(
                new ServiceChallenge("어르신 인터뷰하기", "서로의 삶의 이야기를 들어보는 시간을 가져보세요", "경로당"),
                new ServiceChallenge("문화센터 수업 체험해보기", "새로운 활동을 배우며 친구들과 이야기 나눠보세요", "근처 문화센터")
        ));

        challenges.put(11, List.of(
                new ServiceChallenge("TV 함께 보기", "다같이 TV를 보며 이야기를 나눠보세요", "경로당"),
                new ServiceChallenge("물 많이 마시기 챌린지", "물 많이 마시기 챌린지를 함께 해보세요", "경로당")
        ));

        challenges.put(12, List.of(
                new ServiceChallenge("크리스마스 카드 만들기", "카드를 만들어서 따뜻한 마음을 전해보세요", "경로당 주방"),
                new ServiceChallenge("같이 귤 까먹기", "귤을 까먹으며 대화를 나눠보세요", "경로당")
        ));

        SERVICE_CHALLENGES = Collections.unmodifiableMap(challenges);
    }

    /**
     * 서비스 제공 미션만 생성
     */
    @Scheduled(cron = "0 50 8 17 * ?")  // 초 분 시 일 월
    public void createServiceChallengesForAllSeniorCenters() {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        List<SeniorCenter> allSeniorCenters = seniorCenterRepository.findAll();

        for (SeniorCenter seniorCenter : allSeniorCenters) {
            createServiceChallengesForAllSeniorCenter(seniorCenter, currentYear, currentMonth);
        }
    }

    public void createServiceChallengesForAllSeniorCenter(SeniorCenter seniorCenter, Integer year, Integer month) {
        Long existingServiceChallenges = challengeRepository.countChallengesByYearAndMonthAndType(
                seniorCenter.getId(), year, month, ChallengeType.SERVICE);

        if (existingServiceChallenges > 0) {
            return;
        }

        List<ServiceChallenge> serviceChallenges = SERVICE_CHALLENGES.get(month);
        if (serviceChallenges == null || serviceChallenges.isEmpty()) {
            log.warn("{}월에 대한 서비스 제공 미션이 정의되지 않았습니다.", month);
            return;
        }

        for (ServiceChallenge serviceChallenge : serviceChallenges) {
            Challenge challenge = Challenge.builder()
                    .challengeTitle(serviceChallenge.getTitle())
                    .challengePlace(serviceChallenge.getPlace())
                    .description(serviceChallenge.getDescription())
                    .challengeType(ChallengeType.SERVICE)
                    .isSuccess(false)
                    .seniorCenter(seniorCenter)
                    .build();

            challengeRepository.save(challenge);
        }

        log.info("경로당 {}에 {}년 {}월 서비스 제공 미션 {} 개 생성 완료",
                seniorCenter.getCenterName(), year, month, serviceChallenges.size());
    }

    /**
     * 공통 실행 로직
     */
    public void executeServiceChallengeCreation(Integer year, Integer month) {
        List<SeniorCenter> allSeniorCenters = seniorCenterRepository.findAll();

        for (SeniorCenter seniorCenter : allSeniorCenters) {
            try {
                createServiceChallengesForAllSeniorCenter(seniorCenter, year, month);;
                log.info("경로당 {} 서비스 제공 미션 생성 완료", seniorCenter.getCenterName());
            } catch (Exception e) {
                log.error("경로당 {} 서비스 제공 미션 생성 실패: {}", seniorCenter.getCenterName(), e.getMessage());
            }
        }

        log.info("서비스 제공 미션 자동 생성 완료");
    }


    @Getter
    @AllArgsConstructor
    private static class ServiceChallenge {
        private String title;
        private String description;
        private String place;
    }

}
