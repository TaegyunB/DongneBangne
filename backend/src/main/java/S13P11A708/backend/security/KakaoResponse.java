package S13P11A708.backend.security;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> properties;

    public KakaoResponse(Map<String, Object> attributes){
        this.attributes = attributes;
        this.properties = (Map<String, Object>) attributes.get("properties");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        return properties.get("nickname").toString();
    }

    @Override
    public String getNickname(){
        return properties.get("nickname").toString();
    }

    @Override
    public String getProfileImage(){
        return properties.get("profile_image").toString();
    }

}
