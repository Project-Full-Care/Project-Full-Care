package fullcare.backend.schedule.domain;


import fullcare.backend.global.State;
import fullcare.backend.global.entity.BaseEntity;
import fullcare.backend.member.domain.Member;
import fullcare.backend.project.domain.Project;
import fullcare.backend.projectmember.domain.ProjectMember;
import fullcare.backend.schedulemember.domain.ScheduleMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorColumn(name = "dtype")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity(name="schedule")
@Table(name="schedule")
public abstract class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // todo -> private ProjectMember author;
    private String author;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleMember> scheduleMembers = new ArrayList<>();

    @Column(name = "create_dt", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_dt", nullable = false)
    private LocalDateTime modifiedDate;



    public Schedule(Project project, String author, State state, String title, String content, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.project = project;
        this.author = author;
        this.state = state;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public void addMemberList(List<Member> memberList){
        memberList.forEach(member -> {
            ScheduleMember sm = ScheduleMember.builder()
                    .member(member)
                    .schedule(this)
                    .recentView(LocalDateTime.now()).build();
            scheduleMembers.add(sm);
        });
    }
    public void addMember(Member member){

            ScheduleMember sm = ScheduleMember.builder()
                    .member(member)
                    .schedule(this)
                    .recentView(LocalDateTime.now()).build();
            scheduleMembers.add(sm);

    }


    public void update(State state, String title, String content, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime modifiedDate) {
        this.state = state;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modifiedDate = modifiedDate;
    }

    public void updateState(LocalDateTime modifiedDate, State state){
        this.modifiedDate = modifiedDate;
        this.state = state;
    }
}
