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
                new ServiceChallenge("눈 오는 날 시 쓰기", "눈 오는 날 시 쓰기를 함께 해보세요", "경로당"),
                new ServiceChallenge("올해 목표 종이에 적기", "올해 목표 종이에 적기를 함께 해보세요", "경로당")
        ));

        challenges.put(2, List.of(
                new ServiceChallenge("물 많이 마시기 챌린지", "물 많이 마시기 챌린지를 함께 해보세요", "경로당"),
                new ServiceChallenge("마트에서 반찬거리 장보기", "함께 장을 보며 요리 아이디어도 나눠보세요", "동네 마트")
        ));

        challenges.put(3, List.of(
                new ServiceChallenge("어르신 인터뷰하기", "서로의 삶의 이야기를 들어보는 시간을 가져보세요", "경로당"),
                new ServiceChallenge("강원도 계곡 나들이", "계곡 물소리를 들으며 힐링 시간을 가져보세요", "강원도 계곡")
        ));

        challenges.put(4, List.of(
                new ServiceChallenge("물 많이 마시기 챌린지", "물 많이 마시기 챌린지를 함께 해보세요", "경로당"),
                new ServiceChallenge("근처 뒷산 등산하기", "가벼운 등산으로 건강도 챙기고 자연도 느껴보세요", "동네 뒷산")
        ));

        challenges.put(5, List.of(
                new ServiceChallenge("어르신 인터뷰하기", "서로의 삶의 이야기를 들어보는 시간을 가져보세요", "경로당"),
                new ServiceChallenge("같이 팩하며 피부관리", "서로 팩을 나눠주며 웃음꽃 피워보세요", "경로당")
        ));

        challenges.put(6, List.of(
                new ServiceChallenge("장마철 건강관리법 공유", "서로의 건강을 위해 혈압과 혈당을 체크해보세요", "경로당"),
                new ServiceChallenge("시장 구경하며 간식 사먹기", "시장의 다양한 음식과 분위기를 즐겨보세요", "전통시장")
        ));

        challenges.put(7, List.of(
                new ServiceChallenge("동네 서점 가서 책 한 권 골라보기", "책을 통해 새로운 세상을 만나보세요", "동네 서점"),
                new ServiceChallenge("문화센터 수업 체험해보기", "새로운 활동을 배우며 친구들과 이야기 나눠보세요", "근처 문화센터")
        ));

        challenges.put(8, List.of(
                new ServiceChallenge("물 많이 마시기 챌린지", "물 많이 마시기 챌린지를 함께 해보세요", "경로당"),
                new ServiceChallenge("근처 미용실 가서 헤어 스타일 바꾸기", "새로운 스타일로 기분 전환해보세요", "근처 미용실")
        ));

        challenges.put(9, List.of(
                new ServiceChallenge("고향 이야기 나누기", "고향 이야기 나누기를 함께 해보세요", "경로당"),
                new ServiceChallenge("동네 서점 가서 책 한 권 골라보기", "책을 통해 새로운 세상을 만나보세요", "동네 서점")
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
     * 매월 11일 오전 1시 자동 실행 - 서비스 제공 미션만 생성
     */
    @Scheduled(cron = "0 20 12 11 * ?")  // 매월 11일 오전 1시 (테스트용)
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
