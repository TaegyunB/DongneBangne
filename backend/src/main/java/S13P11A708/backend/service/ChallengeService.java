package S13P11A708.backend.service;

import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.dto.request.challenge.CreateChallengeRequestDto;
import S13P11A708.backend.dto.request.challenge.UpdateChallengeRequestDto;
import S13P11A708.backend.dto.response.challenge.*;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterChallengeListResponseDto;
import S13P11A708.backend.repository.ChallengeRepository;
import S13P11A708.backend.repository.SeniorCenterRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    private static final int MAX_MONTHLY_CHALLENGES = 4;  // 월별 최대 챌린지 개수

    //== 일반 사용자 메서드 ==//

    /**
     * 사용자 경로당의 특정 년월 챌린지 조회
     */
    public List<ChallengeResponseDto> getMonthlyChallenges(Long userId, Integer year, Integer month) {
        SeniorCenter seniorCenter = validateMemberAndGetSeniorCenter(userId);

        List<Challenge> challenges = challengeRepository.findChallengesByYearAndMonth(seniorCenter.getId(), year, month);

        return challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 챌린지 상세 조회
     */
    public ChallengeResponseDto getChallengeDetail(Long challengeId, Long userId) {
        SeniorCenter seniorCenter = validateMemberAndGetSeniorCenter(userId);

        // 사용자의 경로당에 속한 도전만 조회 가능
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 챌린지을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        return ChallengeResponseDto.from(challenge);
    }

    /**
     * 특정 경로당의 특정 년월 챌린지 목록 조회 (공개)
     */
    public SeniorCenterChallengeListResponseDto getSeniorCenterChallengesByYearMonth(Long seniorCenterId, Integer year, Integer month) {
        // 경로당 존재 여부 확인
        SeniorCenter seniorCenter = seniorCenterRepository.findById(seniorCenterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경로당입니다."));

        // 해당 년월의 미션 목록 조회
        List<Challenge> challenges = challengeRepository.findChallengesByYearAndMonth(seniorCenterId, year, month);

        // DTO 변환
        List<ChallengeResponseDto> challengeResponseDtos = challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());

        return SeniorCenterChallengeListResponseDto.builder()
                .seniorCenterId(seniorCenter.getId())
                .seniorCenterName(seniorCenter.getCenterName())
                .address(seniorCenter.getAddress())
                .challenges(challengeResponseDtos)
                .build();
    }

    /**
     * 특정 챌린지 상세 조회 (공개)
     */
    public ChallengeResponseDto getSeniorCenterChallengeDetail(Long challengeId, Long seniorCenterId) {
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenterId);

        if (challenge == null) {
            throw new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다.");
        }

        return ChallengeResponseDto.from(challenge);
    }

    //== Admin 전용 메서드 ==//

    /**
     * 챌린지 생성 (Admin 전용)
     */
    public CreateChallengeResponseDto createChallenge(CreateChallengeRequestDto requestDto, Long adminId) {
        SeniorCenter seniorCenter = validateAdminAndGetSeniorCenter(adminId);

        // 현재 월의 도전 개수 확인
        LocalDateTime now = LocalDateTime.now();
        Integer currentYear = now.getYear();
        Integer currentMonth = now.getMonthValue();

        // 현재 월의 도전 개수 확인
        List<Challenge> existingChallenges = challengeRepository.findChallengesByYearAndMonth(seniorCenter.getId(), currentYear, currentMonth);

        if(existingChallenges.size() > MAX_MONTHLY_CHALLENGES) {
            throw new IllegalArgumentException("이번 달은 이미 최대 개수(" + MAX_MONTHLY_CHALLENGES + "개)의 도전이 생성되었습니다.");
        }

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
     * 챌린지 수정 (Admin 전용)
     */
    public UpdateChallengeResponseDto updateChallenge(Long challengeId, UpdateChallengeRequestDto requestDto, Long adminId) {
        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

        challenge.updateChallengeInfo(
                requestDto.getChallengeTitle(),
                requestDto.getChallengePlace(),
                requestDto.getDescription()
        );

        Challenge updateChallenge = challengeRepository.save(challenge);
        return UpdateChallengeResponseDto.from(updateChallenge);
    }

    /**
     * 챌린지 삭제 (Admin 전용)
     */
    public void deleteChallenge(Long challengeId, Long adminId) {
        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

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
        log.info("챌린지 삭제 완료: challengeId={}, adminId={}", challengeId, adminId);
    }

    /**
     * 챌린지 이미지 및 설명 업로드 (Admin 전용)
     */
    public ChallengeResponseDto uploadChallengeImageWithDescription(Long challengeId, MultipartFile imageFile,
                                                    String imageDescription, Long adminId) {

        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

        try {
            // 새 이미지 업로드
            String imageUrl = s3Service.uploadMultipartFile(imageFile, "images");

            // 이미지 URL과 설명 업데이트
            challenge.updateChallengeImage(imageUrl);
            challenge.updateImageDescription(imageDescription);

            Challenge savedChallenge = challengeRepository.save(challenge);

            return ChallengeResponseDto.from(savedChallenge);

        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 챌린지 이미지 업로드 (Admin 전용)
     */
    public void updateChallengeImage(Long challengeId, String imageUrl, Long adminId) {
        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

        challenge.updateChallengeImage(imageUrl);
        challengeRepository.save(challenge);
    }

    /**
     * 챌린지 이미지 삭제 (Admin 전용)
     */
    public void deleteChallengeImage(Long challengeId, Long adminId) {
        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

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
     * 챌린지 완료 처리 (Admin 전용)
     */
    public CompleteChallengeResponseDto completeChallenge(Long challengeId, Long adminId) {
        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

        // 이미 완료되었는지 확인
        if (challenge.getIsSuccess()) {
            throw new IllegalArgumentException("해당 챌린지는 이미 완료되었습니다.");
        }

        // 완료 조건 검증: 이미지와 설명이 모두 있어야 함
        if (challenge.getChallengeImage() == null || challenge.getChallengeImage().isEmpty()) {
            throw new IllegalArgumentException("챌린지를 완료하려면 인증 사진을 업로드해주세요.");
        }

        if (challenge.getImageDescription() == null || challenge.getImageDescription().isEmpty()) {
            throw new IllegalArgumentException("챌린지를 완료하려면 사진에 대한 설명을 작성해주세요.");
        }

        challenge.completeChallenge();
        Challenge completedChallenge = challengeRepository.save(challenge);

        SeniorCenter seniorCenter = validateAdminAndGetSeniorCenter(adminId);

        // 경로당 포인트 추가
        seniorCenter.addChallengePoint(challenge.getPoint());
        SeniorCenter updatedSeniorCenter = seniorCenterRepository.save(seniorCenter);

        return CompleteChallengeResponseDto.from(completedChallenge, updatedSeniorCenter);
    }


    /**
     * 챌린지 완료 취소 (Admin 전용)
     */
    public CancelCompletedChallengeResponseDto cancelChallengeCompletion(Long challengeId, Long adminId) {
        Challenge challenge = validateAndGetChallenge(challengeId, adminId);

        // 완료되지 않은 도전인지 확인
        if (!challenge.getIsSuccess()) {
            throw new IllegalArgumentException("완료되지 않은 챌린지입니다.");
        }

        // 도전 완료 취소
        challenge.cancelCompletion();
        Challenge canceledChallenge = challengeRepository.save(challenge);

        SeniorCenter seniorCenter = validateAdminAndGetSeniorCenter(adminId);
        // 경로당 포인트 차감
        seniorCenter.subtractChallengePoint(challenge.getPoint());
        SeniorCenter updatedSeniorCenter = seniorCenterRepository.save(seniorCenter);

        return CancelCompletedChallengeResponseDto.from(canceledChallenge, updatedSeniorCenter);
    }

    //== 공통 검증 메서드 ==//

    /**
     * 회원 권한 및 경로당 소속 검증
     */
    private SeniorCenter validateMemberAndGetSeniorCenter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        SeniorCenter seniorCenter = user.getSeniorCenter();
        if(seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 사용자입니다.");
        }

        return seniorCenter;
    }

    /**
     * 경로당 관리자 권환 확인
     */
    private SeniorCenter validateAdminAndGetSeniorCenter(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        if (admin.getUserRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        SeniorCenter seniorCenter = admin.getSeniorCenter();
        if (seniorCenter == null) {
            throw new IllegalArgumentException("경로당에 소속되지 않은 관리자입닌다.");
        }

        if (seniorCenter.getAdminUserId() == null) {
            throw new IllegalArgumentException("해당 경로당에 관리자가 설정되지 않았습니다.");
        }

        if (!seniorCenter.getAdminUserId().equals(adminId)) {
            throw new IllegalArgumentException("해당 경로당의 관리자가 아닙니다.");
        }

        return seniorCenter;
    }

    /**
     * 관리자 권한 검증 후 챌린지 조회
     */
    private Challenge validateAndGetChallenge(Long challengeId, Long adminId) {
        SeniorCenter seniorCenter = validateAdminAndGetSeniorCenter(adminId);

        // 도전 존재 및 권환 확인
        Challenge challenge = challengeRepository.findChallengeByIdAndSeniorCenterId(challengeId, seniorCenter.getId());
        if (challenge == null) {
            throw new IllegalArgumentException("해당 도전을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        return challenge;
    }

    //== 헬퍼 메서드 ==//

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
