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

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User.getAttributes());

        //어떤 인증 서버인지(카카오, 네이버, 구글 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if(registrationId.equals("kakao")){
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }else{
            return null;
        }

        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();

        User existData = userRepository.findByUserName(username);
        UserRole defaultRole = UserRole.MEMBER;
        if(existData == null){
            User user = new User();

            user.setUserName(oAuth2Response.getName()); //user_name을 저장 ex.주연우
            user.setNickname(username); // kakao 1234567
            user.setProfileImage(oAuth2Response.getProfileImage());
            user.setUserRole(defaultRole); //MEMBER로 디폴트

            userRepository.save(user);
        } else {
            UserRole role = existData.getUserRole();

            userRepository.save(existData);
        }

        return new CustomOAuth2User(oAuth2Response, defaultRole);

    }
}
