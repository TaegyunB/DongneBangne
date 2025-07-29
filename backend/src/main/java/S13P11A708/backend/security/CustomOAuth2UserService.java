package S13P11A708.backend.security;

import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        //카카오 로그인 정보 받아오기
        //Map<String, Object> 형태
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User.getAttributes());

        //어떤 인증 서버인지 확인(카카오, 네이버, 구글 등)
        //동네방네는 kakao
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if(registrationId.equals("kakao")){
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }else{
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth Provider 입니다.");
        }

        String kakaoId = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId(); // kakao 1234567
        String nickname = oAuth2Response.getName();
        String profileImage = oAuth2Response.getProfileImage();
        User existData = userRepository.findByKakaoId(kakaoId); //DB에 동일한 kakaoId을 가진 유저가 있는지 확인
        UserRole defaultRole = UserRole.GUEST;

        // 신규/기존 회원 구분
        if(existData == null){ // DB에 해당 유저가 없을 경우 -> 신규 사용자 -> 회원가입
            User user = new User();

            user.setKakaoId(kakaoId); // 소셜 아이디 ex. kakao 1234567
            user.setNickname(oAuth2Response.getName()); //user_name에 OAuth의 properties 중 nickname을 저장 ex.주연우
            user.setProfileImage(oAuth2Response.getProfileImage());
            user.setUserRole(defaultRole); //GUEST로 디폴트

            User savedUser = userRepository.save(user);
//            userRepository.save(user);

            UserDto userDTO = new UserDto();
            userDTO.setUserId(savedUser.getId());
            userDTO.setKakaoId(kakaoId);
            userDTO.setNickname(oAuth2Response.getName());
            userDTO.setProfileImage(oAuth2Response.getProfileImage());
            userDTO.setUserRole(defaultRole);

            return new CustomOAuth2User(userDTO);
        } else {
            //이미 값이 있는 경우 -> 기존 사용자 -> 수정/갱신
            //kakao ID는 고유하므로 set 설정해주지 않음
            existData.setNickname(oAuth2Response.getName());
            existData.setProfileImage(oAuth2Response.getProfileImage());

            userRepository.save(existData);

            UserDto userDTO = new UserDto();
            userDTO.setUserId(existData.getId());
            userDTO.setKakaoId(existData.getKakaoId());
            userDTO.setNickname(oAuth2Response.getName());
            userDTO.setProfileImage(oAuth2Response.getProfileImage());
            userDTO.setUserRole(existData.getUserRole());

            return new CustomOAuth2User(userDTO);
        }
    }
}
