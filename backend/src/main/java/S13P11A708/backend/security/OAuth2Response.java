package S13P11A708.backend.security;

public interface OAuth2Response {

    String getProvider(); //제공자
    String getProviderId(); //제공자에서 발급해주는 아이디(번호)
    String getName(); //사용자 설정 이름
    String getProfileImage(); //사용자 프로필 사진
}
