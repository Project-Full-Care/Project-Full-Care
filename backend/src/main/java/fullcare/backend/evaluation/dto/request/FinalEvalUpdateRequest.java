package fullcare.backend.evaluation.dto.request;

import fullcare.backend.evaluation.domain.Score;
import fullcare.backend.global.State;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Getter
public class FinalEvalUpdateRequest {
    @NotNull
    private Long projectId;
    @NotNull
    private Score score;
    @NotNull
    private String content;
    @NotNull
    private State state;
}
