package S13P11A708.backend.service;

import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.request.challenge.ChallengeCreateRequestDto;
import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import S13P11A708.backend.repository.ChallengeRepository;
import S13P11A708.backend.repository.SeniorCenterRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChallengeService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    private static final int MAX_MONTHLY_CHALLENGES = 4;  // 월별 최대 도전 개수

    /**
     * 도전 생성 (Admin 전용)
     */
    public ChallengeResponseDto createChallenge(ChallengeCreateRequestDto requestDto, Long adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        // 경로당 조회 및 관리자 권한 확인
        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if (seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        validateAdminPermission(seniorCenter, adminId);

        // DTO -> Entity 권한
        Challenge challenge = Challenge.builder()
                .challengeTitle(requestDto.getChallengeTitle())
                .challengePlace(requestDto.getChallengePlace())
                .description(requestDto.getDescription())
                .isSuccess(false)
                .seniorCenter(seniorCenter)
                .build();

        // Entity 저장
        Challenge savedChallenge = challengeRepository.save(challenge);

        // Entity -> DTO 변환 후 반환
        return ChallengeResponseDto.from(savedChallenge);
    }

    /**
     * 경로당 관리자 권환 확인
     */
    private void validateAdminPermission(SeniorCenter seniorCenter, Long adminId) {
        Long adminUserId = seniorCenter.getAdminUserId();

        // 디버깅
        log.info("JWT에서 추출한 adminId: {}", adminId);
        log.info("DB의 adminUserId: {}", adminUserId);
        log.info("seniorCenterId: {}", seniorCenter.getId());

        if (adminUserId == null) {
            throw new IllegalArgumentException("해당 경로당에 관리자가 설정되지 않았습니다.");
        }

        if(!seniorCenter.getAdminUserId().equals(adminId)) {
            throw new IllegalArgumentException("해당 경로당의 관리자만 도전을 관리할 수 있습니다.");
        }
    }


}
