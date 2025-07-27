package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import S13P11A708.backend.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 카카오 고유 ID (OAuth에서 제공)
    @Column(name = "kakao_id", unique = true, nullable = false)
    private Long kakaoId;

    // 카카오 계정 이메일 (OAuth에서 제공 - 동의 시)
    @Column(name = "email")
    private String email;

    // 카카오 닉네임 (OAuth에서 제공)
    private String nickname;

    // 카카오 썸네일 이미지 URL (OAuth에서 제공)
    @Column(name = "profile_image")
    private String profileImage;

   // @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private String userRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_center_id")
    private SeniorCenter seniorCenter;

    //jpa에서 자동 처리해서 builder에서 호출 필요 없게
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 카카오 닉네임에서 제공하는 닉네임 말고 커뮤니티에서 사용하는 유저네임을 저장해야하지 않을까
//    @Column(name = "user_name", nullable = false)
//    private String userName;


    //==Setter 메서드==//

    //==조회 메서드==//
    //public boolean isAdmin() {
    //    return this.userRole == UserRole.ADMIN;
    //}
}
