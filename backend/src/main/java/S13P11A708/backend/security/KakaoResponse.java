package S13P11A708.backend.security;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> properties;

    //카카오 oauth에 맞는 정보 가져오는 메서드
    public KakaoResponse(Map<String, Object> attributes){
        this.attributes = attributes;
        this.properties = (Map<String, Object>) attributes.get("properties");
    }

    //소셜 로그인 제공자 식별 (kakao)
    @Override
    public String getProvider() {
        log.info((String) attributes.get("key"));
        return "kakao";
    }

    //카카오 계정 고유 ID (1234567890)
    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    //카카오 반환값 중 properties 중 nickname 반환 (홍길동)
    @Override
    public String getName() {
        return properties.get("nickname").toString();
    }

    //프로필 이미지 url (.jpg)
    @Override
    public String getProfileImage(){
        return properties.get("profile_image").toString();
    }

}
