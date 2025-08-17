package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import S13P11A708.backend.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
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

    // 카카오 id
    @Column(name = "kakao_id")
    private String kakaoId;

    // 카카오 닉네임 (OAuth에서 제공)
    @Column(name = "nickname", nullable = false)
    private String nickname;

    // 카카오 썸네일 이미지 URL (OAuth에서 제공)
    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    @Column(name = "personal_point")
    @Builder.Default
    private Long personalPoint = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_center_id")
    private SeniorCenter seniorCenter;

    //jpa에서 자동 처리해서 builder에서 호출 필요 없게
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    //==Setter 메서드==//
    public Long addPoint(Long currentPoint){
        return this.personalPoint = currentPoint;
    }


    //==조회 메서드==//
    //public boolean isAdmin() {
    //    return this.userRole == UserRole.ADMIN;
    //}
}


