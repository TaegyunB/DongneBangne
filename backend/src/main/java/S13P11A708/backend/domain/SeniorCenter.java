package S13P11A708.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.ConcreteProxy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "senior_center")
public class SeniorCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "senior_center_id")
    private Long id;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "center_name", nullable = false)
    private String centerName;

    @Column(nullable = false)
    private String address;

    @Column(name = "trot_point")
    private Long trotPoint = 0L;

    @Column(name = "word_point")
    private Long wordPoint = 0L;

    @Column(name = "talk_point")
    private Long talkPoint = 0L;

    @Column(name = "challenge_point")
    private Long challengePoint = 0L;

    @Column(name = "total_point")
    private Long totalPoint = 0L;

    // 경로당 소속 회원들 - mappedBy로 User의 seniorCenter 필드와 연결
    @OneToMany(mappedBy = "seniorCenter", fetch = FetchType.LAZY)
    private List<User> members = new ArrayList<>();

    // 경로당들의 랭킹 기록
    @OneToMany(mappedBy = "seniorCenter", fetch = FetchType.LAZY)
    private List<Ranking> rankings = new ArrayList<>();

    @OneToMany(mappedBy = "seniorCenter", fetch = FetchType.LAZY)
    private List<Challenge> challenges = new ArrayList<>();

    //==Setter 메서드==//
    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }


}
