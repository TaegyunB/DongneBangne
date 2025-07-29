package S13P11A708.backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserDto userDTO;

    public CustomOAuth2User(UserDto userDTO){
        this.userDTO = userDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }
    
    //유저 Role을 spring security에 알려주는 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(() -> "ROLE_" + userDTO.getUserRole().name());
//        collection.add(new GrantedAuthority() {
//            @Override
//            public String getAuthority() {
//
//                return userDTO.getUserRole().toString();
//            }
//        });

        return collection;
    }
    
    // properties의 nickname 홍길동 반환
    @Override
    public String getName() {
        return userDTO.getNickname();
    }
    
    // DB의 user_name과 연결됨. kakao 고유 id 가져옴.
    public String getUsername(){
        return userDTO.getKakaoId();
    }

    // 동네방네 user Id
    public Long getUserId(){
        return userDTO.getUserId();
    }


}
