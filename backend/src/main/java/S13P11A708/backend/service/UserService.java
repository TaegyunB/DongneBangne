package S13P11A708.backend.service;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.dto.request.user.UpdateProfileRequestDto;
import S13P11A708.backend.dto.response.user.UserProfileResponseDto;
import S13P11A708.backend.jwt.JWTUtil;
import S13P11A708.backend.repository.SeniorCenterRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SeniorCenterRepository seniorCenterRepository;
    private final JWTUtil jwtUtil;

    // userId 기준으로 유저 조회
    // 해당 유저가 경로당에 소속되어 있는지 확인
    @Transactional
    public boolean hasCenter(Long userId){
        return userRepository.findById(userId)
                .map(user -> user.getSeniorCenter() != null)
                .orElse(false);
    }

    //GUEST 상태의 유저가 경로당을 선택하면
    //역할을 ADMIN, MEMBER로 바꾸고
    //새로운 JWT 토큰 발급해서 CLIENT에 반환
    @Transactional
    public String assignCenter(Long userId, Long seniorCenterId){
        User user = userRepository.findById(userId) //DB에서 유저 조회
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        SeniorCenter center = seniorCenterRepository.findById(seniorCenterId) //DB에서 경로당 조회
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경로당입니다."));

        // 경로당에 이미 소속된 사용자가 있는지 확인
        //countBy가 아닌 existsBy를 사용하여 속도를 높임
        boolean alreadyExists = userRepository.existsBySeniorCenterId(center.getId());

        //ADMIN 권한 부여
        //경로당의 첫번째 멤버이면 ADMIN이 됨
        user.setSeniorCenter(center);
        user.setUserRole(alreadyExists ? UserRole.MEMBER : UserRole.ADMIN);

        //jwt 재발급
        return jwtUtil.createJwt(user.getId(), user.getUserRole().name(), 1000L * 60 * 60 * 24);
    }

    @Transactional
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        return UserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }

    @Transactional
    public UserProfileResponseDto updateUserProfile(Long userId, UpdateProfileRequestDto request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        user.setNickname(request.getNickname());
        user.setProfileImage(request.getProfileImage());

        userRepository.save(user);

        return UserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }
}
