package fullcare.backend.member.domain;

import fullcare.backend.evaluation.domain.Evaluation;
import fullcare.backend.likes.domain.Likes;
import fullcare.backend.post.domain.Post;
import fullcare.backend.projectmember.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate // ! 더티 체킹할 때 바뀐 필드만 변경
@Table(name = "member")
@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "oauth2_id")
    private String oAuth2Id;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role;

    @Column(name = "signup_date")
    private LocalDateTime signupDate;

    @Column(name = "refreshToken")
    private String refreshToken;

    //    @Column
//    private String profileContent;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<Likes> likes = new HashSet<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Evaluation> evaluations = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProjectMember> projectMembers = new ArrayList<>();




    public void updateOAuth2Id(String oAuth2Id) {
        this.oAuth2Id = oAuth2Id;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
