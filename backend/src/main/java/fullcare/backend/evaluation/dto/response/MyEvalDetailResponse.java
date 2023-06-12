package fullcare.backend.evaluation.dto.response;

import fullcare.backend.evaluation.dto.BadgeDto;
import fullcare.backend.evaluation.dto.FinalEvalDto;
import fullcare.backend.evaluation.dto.ScoreDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class MyEvalDetailResponse {
    private List<BadgeDto> badgeDtos = new ArrayList<>();
    private List<FinalEvalDto> finalEvals = new ArrayList();
    private ScoreDto score;

    public MyEvalDetailResponse(List<BadgeDto> badgeDtos, List<FinalEvalDto> finalEvals, ScoreDto scoreDto) {
        this.badgeDtos = badgeDtos;
        this.finalEvals = finalEvals;
        this.score = scoreDto;
    }
}
