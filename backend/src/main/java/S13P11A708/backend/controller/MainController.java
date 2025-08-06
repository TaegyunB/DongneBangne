package S13P11A708.backend.controller;


import S13P11A708.backend.dto.response.user.UserResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/main")
public class MainController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        UserResponseDto currentUser = userService.getCurrentUser(customUser.getUserId());
        return ResponseEntity.ok(currentUser);

    }


}
