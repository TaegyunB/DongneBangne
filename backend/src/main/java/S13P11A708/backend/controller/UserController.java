package S13P11A708.backend.controller;

import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;

    //소속 경로당 유무 체크
//    @GetMapping("/senior-center")
//    public ResponseEntity<String> checkSeniorCenter(@AuthenticationPrincipal CustomOAuth2User oAuth2User){
//        String username = oAuth2User.getUsername();
//    }

}
