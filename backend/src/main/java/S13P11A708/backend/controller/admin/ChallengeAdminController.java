package S13P11A708.backend.controller.admin;

import S13P11A708.backend.dto.request.challenge.CreateChallengeCreateRequestDto;
import S13P11A708.backend.dto.response.challenge.CreateChallengeResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/challenges")
public class ChallengeAdminController {

    private final ChallengeService challengeService;

    /**
     * 도전 생성
     * @param requestDto 도전 생성 요청 DTO
     * @param adminId JWT 토큰에서 추출한 관리자 ID
     */
    @PostMapping
    public ResponseEntity<CreateChallengeResponseDto> createChallenge(
            @Valid @RequestBody CreateChallengeCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        Long adminId = customUser.getUserId();

        CreateChallengeResponseDto response = challengeService.createChallenge(requestDto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
