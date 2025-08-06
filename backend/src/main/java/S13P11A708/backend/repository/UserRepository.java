package S13P11A708.backend.repository;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;

import java.util.Optional;

@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Long> {
    User findByKakaoId(String kakoId);
    boolean existsBySeniorCenterId(Long seniorCenterId);

    /**
     *
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.seniorCenter WHERE u.id = :userId")
    Optional<User> findByIdWithSeniorCenter(@Param("userId") Long userId);

    @Query("SELECT u.personalPoint FROM User u WHERE u.id = :userId")
    Long findPointByUserId(@Param("userId") Long userId);

}
