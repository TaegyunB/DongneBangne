package S13P11A708.backend.service;

import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.request.challenge.CreateChallengeRequestDto;
import S13P11A708.backend.dto.request.challenge.UpdateChallengeRequestDto;
import S13P11A708.backend.dto.response.challenge.*;
import S13P11A708.backend.repository.ChallengeRepository;
import S13P11A708.backend.repository.SeniorCenterRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChallengeService {

    private final UserRepository userRepository;
    private final SeniorCenterRepository seniorCenterRepository;
    private final ChallengeRepository challengeRepository;
    private final S3Service s3Service;

    private static final int MAX_MONTHLY_CHALLENGES = 4;  // 월별 최대 도전 개수

    /**
     * 사용자 경로당의 특정 년월 미션 조회
     */
    public List<ChallengeResponseDto> getMonthlyChallenges(Long userId, Integer year, Integer month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        SeniorCenter seniorCenter = user.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        List<Challenge> challenges = challengeRepository.findChallengesByYearAndMonth(seniorCenter.getId(), year, month);

        return challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());

    }

    /**
     * 특정 도전 상세 조회
     */
    public ChallengeResponseDto getChallengeDetail(Long challengeId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        SeniorCenter seniorCenter = user.getSeniorCenter();
        if (seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        // 사용자의 경로당에 속한 도전만 조회 가능
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        return ChallengeResponseDto.from(challenge);
    }


    //== Admin 전용 ==//

    /**
     * 도전 생성 (Admin 전용)
     */
    public CreateChallengeResponseDto createChallenge(CreateChallengeRequestDto requestDto, Long adminId) {

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
        return CreateChallengeResponseDto.from(savedChallenge);
    }

    /**
     * 도전 수정 (Admin 전용)
     */
    public UpdateChallengeResponseDto updateChallenge(Long challengeId, UpdateChallengeRequestDto requestDto, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if (seniorCenter == null) {
           throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        validateAdminPermission(seniorCenter, adminId);

        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        challenge.updateChallengeInfo(
                requestDto.getChallengeTitle(),
                requestDto.getChallengePlace(),
                requestDto.getDescription()
        );

        Challenge updateChallenge = challengeRepository.save(challenge);
        return UpdateChallengeResponseDto.from(updateChallenge);
    }

    /**
     * 도전 삭제 (Admin 전용)
     */
    public void deleteChallenge(Long challengeId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        // 관리자 권한 검증
        validateAdminPermission(seniorCenter, adminId);

        // 해당 경로당의 도전인지 확인
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // S3에서 이미지 삭제 (이미지가 있는 경우)
        if (challenge.getChallengeImage() != null && !challenge.getChallengeImage().isEmpty()) {
            try {
                // URL에서 파일명 추출
                String imageUrl = challenge.getChallengeImage();
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                s3Service.deleteFile("images/" + fileName);
                log.info("S3에서 이미지 삭제 완료: {}", fileName);
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 중 오류 발생: {}", e.getMessage());
            }
        }

        challengeRepository.delete(challenge);
        log.info("도전 삭제 완료: challengeId={}, adminId={}", challengeId, adminId);
    }

    /**
     * 도전 미션 이미지 업로드 (Admin 전용)
     */
    public void updateChallengeImage(Long challengeId, String imageUrl, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        // 관리자 권한 검증
        validateAdminPermission(seniorCenter, adminId);

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 챌린지입니다."));

        challenge.updateChallengeImage(imageUrl);
        challengeRepository.save(challenge);

    }

    /**
     * 도전 이미지 삭제 (Admin 전용)
     */
    public void deleteChallengeImage(Long challengeId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if (seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        validateAdminPermission(seniorCenter, adminId);

        // 도전 존재 및 권환 확인
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 이미지가 없는 경우
        if (challenge.getChallengeImage() == null || challenge.getChallengeImage().isEmpty()) {
            throw new IllegalArgumentException("삭제할 이미지가 없습니다.");
        }

        try {
            String imageUrl = challenge.getChallengeImage();
            String fileName = extractFileNameFromUrl(imageUrl);
            s3Service.deleteFile("images/" + fileName);
            log.info("S3에서 이미지 삭제 완료: {}", fileName);
        } catch (Exception e) {
            log.error("S3 이미지 삭제 중 오류 발생; {}", e.getMessage());
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다.");
        }

        // DB에서 이미지 URL 제거
        challenge.updateChallengeImage(null);
        challengeRepository.save(challenge);
    }


    /**
     * 도전 완료 처리 (Admin 전용)
     */
    public CompleteChallengeResponseDto completeChallenge(Long challengeId, Long adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        validateAdminPermission(seniorCenter, adminId);

        // 해당 경로당의 도전인지 확인
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 이미 완료되었는지 확인
        if (challenge.getIsSuccess()) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        challenge.completeChallenge();
        Challenge completedChallenge = challengeRepository.save(challenge);

        // 경로당 포인트 추가
        seniorCenter.addChallengePoint(challenge.getPoint());
        SeniorCenter updatedSeniorCenter = seniorCenterRepository.save(seniorCenter);

        return CompleteChallengeResponseDto.from(completedChallenge, updatedSeniorCenter);
    }

    /**
     * 도전 완료 취소 (Admin 전용)
     */
    public CancelCompletedChallengeResponseDto cancelChallengeCompletion(Long challengeId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        validateAdminPermission(seniorCenter, adminId);

        // 도전 존재 및 권환 확인
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 완료되지 않은 도전인지 확인
        if (!challenge.getIsSuccess()) {
            throw new IllegalArgumentException("완료되지 않은 도전입니다.");
        }

        // 도전 완료 취소
        challenge.cancelCompletion();
        Challenge canceledChallenge = challengeRepository.save(challenge);

        // 경로당 포인트 차감
        seniorCenter.subtractChallengePoint(challenge.getPoint());
        SeniorCenter updatedSeniorCenter = seniorCenterRepository.save(seniorCenter);

        return CancelCompletedChallengeResponseDto.from(canceledChallenge, updatedSeniorCenter);
    }

    //== 비즈니스 로직 ==//
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

    /**
     * URL에서 파일명 추출 헬퍼 메서드
     */
    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
        }

        // URL에서 마지막 '/' 뒤의 파일명 추출
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        if (lastSlashIndex == -1 || lastSlashIndex == imageUrl.length() - 1) {
            throw new IllegalArgumentException("파일명을 추출할 수 없는 URL입니다.");
        }

        return imageUrl.substring(lastSlashIndex + 1);
    }
}
