package S13P11A708.backend.repository;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;

@EnableJpaRepositories
public interface
UserRepository extends JpaRepository<User, Long> {

    User findByUserName(String userName);
    boolean existsBySeniorCenterId(Long seniorCenterId);
}
