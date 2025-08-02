package S13P11A708.backend.controller.admin;

import S13P11A708.backend.dto.request.challenge.CreateChallengeRequestDto;
import S13P11A708.backend.dto.request.challenge.UpdateChallengeRequestDto;
import S13P11A708.backend.dto.response.challenge.CancelCompletedChallengeResponseDto;
import S13P11A708.backend.dto.response.challenge.CompleteChallengeResponseDto;
import S13P11A708.backend.dto.response.challenge.CreateChallengeResponseDto;
import S13P11A708.backend.dto.response.challenge.UpdateChallengeResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.ChallengeService;
import S13P11A708.backend.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/challenges")
public class ChallengeAdminController {

    private final ChallengeService challengeService;
    private final S3Service s3Service;

    /**
     * 도전 생성
     * @param requestDto 도전 생성 요청 DTO
     * @param customUser JWT 토큰에서 추출한 관리자 ID
     */
    @PostMapping
    public ResponseEntity<CreateChallengeResponseDto> createChallenge(
            @Valid @RequestBody CreateChallengeRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        CreateChallengeResponseDto response = challengeService.createChallenge(requestDto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 도전 수정
     */
    @PutMapping("/{challengeId}")
    public ResponseEntity<UpdateChallengeResponseDto> updateChallenge(
            @PathVariable("challengeId") Long challengeId,
            @Valid @RequestBody UpdateChallengeRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        UpdateChallengeResponseDto response = challengeService.updateChallenge(challengeId, requestDto, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * 도전 삭제
     */
    @DeleteMapping("/{challengeId}")
    public ResponseEntity<Map<String, String>> deleteChallenge(
            @PathVariable("challengeId") Long challengeId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        challengeService.deleteChallenge(challengeId, adminId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "도전이 성공적으로 삭제되었습니다.");
        response.put("deletedChallengeId", challengeId.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * 챌린지 이미지 업로드
     */
    @PostMapping("/{challengeId}/image")
    public ResponseEntity<Map<String, String>> uploadChallengeImage(
            @PathVariable("challengeId") Long challengeId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomOAuth2User customUser) throws IOException {

        Long adminId = customUser.getUserId();

        // 파일 유효성 검사
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 이미지 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 검사 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // S3에 업로드
        String fileUrl = s3Service.uploadFile(file, "images");

        // DB에 이미지URL 업데이트
        challengeService.updateChallengeImageUrl(challengeId, fileUrl, adminId);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("message", "챌린지 이미지 업로드가 완료되었습니다.");
        response.put("fileName", file.getOriginalFilename());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 챌린지 이미지 삭제
     */
    @DeleteMapping("/{challengeId}/image")
    public ResponseEntity<Map<String, String>> deleteChallengeImage(
            @RequestParam("fileName") String fileName,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        // 파일 삭제
        s3Service.deleteFile("images/" + fileName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "이미지가 성공적으로 삭제되었습니다.");
        response.put("deletedFileName", fileName);

        return ResponseEntity.ok(response);
    }

    /**
     * 도전 완료 처리
     */
    @PutMapping("/{challengeId}/complete")
    public ResponseEntity<CompleteChallengeResponseDto> completeChallenge(
            @PathVariable("challengeId") Long challengeId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        CompleteChallengeResponseDto response = challengeService.completeChallenge(challengeId, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * 도전 완료 취소 (Admin 전용)
     */
    @PutMapping("/{challengeId}/cancel")
    public ResponseEntity<CancelCompletedChallengeResponseDto> cancelCompletedChallengne(
            @PathVariable("challengeId") Long challengeId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        CancelCompletedChallengeResponseDto response = challengeService.cancelChallengeCompletion(challengeId, adminId);
        return ResponseEntity.ok(response);
    }
}
