package S13P11A708.backend.controller;

import S13P11A708.backend.domain.User;
import S13P11A708.backend.dto.request.user.SelectCenterRequestDto;
import S13P11A708.backend.dto.response.user.CenterStatusResponseDto;
import S13P11A708.backend.dto.response.user.SelectCenterResponseDto;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    //로그인 -> 신규 가입(소속 경로당 없음) -> 소속 경로당 선택
    //로그인 -> 기존 회원 -> 메인페이지
    //소속 경로당 존재 여부 확인 API
    @GetMapping("/senior-center")
    public ResponseEntity<CenterStatusResponseDto> hasCenter(@AuthenticationPrincipal CustomOAuth2User oAuth2User){
        Long userId = oAuth2User.getUserId(); //동네방네 userId
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

        boolean hasCenter = user.getSeniorCenter() != null;

        if(hasCenter){
            return new ResponseEntity<>(new CenterStatusResponseDto(true, user.getNickname()+"님은"+user.getSeniorCenter()+"에 소속되어 있습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new CenterStatusResponseDto(false,"소소된 경로당이 없습니다."), HttpStatus.OK);
        }
    }

    //경로당 선택 API
    @PostMapping("/senior-center")
    public ResponseEntity<SelectCenterResponseDto> selectCenter(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                @RequestBody SelectCenterRequestDto request){

        Long userId = oAuth2User.getUserId(); //로그인 된 유저
        boolean isMember = userService.assignCenter(userId, request.getSeniorCenterId()); //프론트에서 받은 seniorCenterId

        if(isMember){
            return new ResponseEntity<>(new SelectCenterResponseDto(true, "경로당 등록 완료"), HttpStatus.CREATED);
        }else{
            return new ResponseEntity<>(new SelectCenterResponseDto(true, "경로당 등록 및 ADMIN 지정 완료"), HttpStatus.CREATED);
        }
    }

}
