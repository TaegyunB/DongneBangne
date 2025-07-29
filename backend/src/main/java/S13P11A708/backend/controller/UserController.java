package S13P11A708.backend.controller;

import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.response.user.SeniorCenterStatusResponseDto;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;

//    //소속 경로당 유무 체크
//    @GetMapping("/{userId}/senior-center")
//    public ResponseEntity<SeniorCenterStatusResponseDto> checkSeniorCenter(@AuthenticationPrincipal CustomOAuth2User oAuth2User){
//        Long userId = oAuth2User.getUserId(); //동네방네 userId
//        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));
//
//        boolean hasSeniorCenter = user.getSeniorCenter() != null;
//
//        return ResponseEntity.ok(new SeniorCenterStatusResponseDto(hasSeniorCenter));
//    }

}
