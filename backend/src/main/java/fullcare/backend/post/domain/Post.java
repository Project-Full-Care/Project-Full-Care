package fullcare.backend.post.domain;

import fullcare.backend.global.State;
import fullcare.backend.global.entity.BaseEntity;
import fullcare.backend.likes.domain.Likes;
import fullcare.backend.projectmember.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString(of = {"id", "title", "recruitments"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "project_id", nullable = false)
//    private Project project;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id", nullable = false)
//    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private ProjectMember projectMember;


    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "reference", nullable = false)
    private String reference;

    @Lob
    @Column(name = "contact", nullable = false)
    private String contact;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Lob
    @Column(name = "region", nullable = false)
    private String region;

    @Lob
    @Column(name = "tech_stack", nullable = false)
    private String techStack;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recruitment> recruitments = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Likes> likes = new HashSet<>();  // * 좋아요 갯수는 likes set의 size를 이용

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Builder(builderMethodName = "createNewPost")
    public Post(ProjectMember projectMember, String title, String reference, String contact, String description, String region, String techStack, State state) {
        this.projectMember = projectMember;
        this.title = title;
        this.reference = reference;
        this.contact = contact;
        this.description = description;
        this.region = region;
        this.techStack = techStack;
        this.state = state;
    }

    public void updateAll(String title, String reference, String contact, String description, String region, List<Recruitment> recruitments) {
        this.title = title;
        this.reference = reference;
        this.contact = contact;
        this.description = description;
        this.region = region;

        updateRecruit(recruitments);
    }

    public void updateRecruit(List<Recruitment> recruitments) {
        this.recruitments.clear();
        this.recruitments.addAll(recruitments);
    }

}
