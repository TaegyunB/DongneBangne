package S13P11A708.backend.service;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.UserRole;
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

    // userId 기준으로 유저 조회
    // 해당 유저가 경로당에 소속되어 있는지 확인
    public boolean hasCenter(Long userId){
        return userRepository.findById(userId)
                .map(user -> user.getSeniorCenter() != null)
                .orElse(false);
    }

    @Transactional
    public boolean assignCenter(Long userId, Long seniorCenterId){
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

        return alreadyExists;
    }


}
