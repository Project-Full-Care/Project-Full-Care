package fullcare.backend.evaluation.domain;


import fullcare.backend.member.domain.Member;
import fullcare.backend.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FinalTermEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "final_eval_id")
    private Long id;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Embedded
    private Score score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Member evaluator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_id", nullable = false)
    private Member evaluated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder(builderMethodName = "createNewFinalEval")
    public FinalTermEvaluation(String content, Score score, Member evaluator, Member evaluated, Project project) {
        this.content = content;
        this.score = score;
        this.evaluator = evaluator;
        this.evaluated = evaluated;
        this.project = project;
    }
}