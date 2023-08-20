package fullcare.backend.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import fullcare.backend.schedule.ScheduleCategory;
import fullcare.backend.schedule.dto.MemberDto;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleCreateRequest {

    @NotNull
    private Long projectId;

    // TODO startDate(이전) < endDate(이후) + NotBlank인가 NotEmpty인가?
    @FutureOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startDate;

    @FutureOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;

    // ? NotBlank인가 NotEmpty인가?
    private ScheduleCategory category;

    @NotEmpty
    private List<Long> pmIds;

    @NotBlank
    @Length(min = 2, max = 20)
    private String title;

    @NotEmpty
    private String content;

//    @NotNull
    private String address;


    public ScheduleCreateRequest(Long projectId, LocalDateTime startDate, LocalDateTime endDate, ScheduleCategory category, List<Long> pmIds, String title, String content, String address) {
        this.projectId = projectId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.pmIds = pmIds;
        this.title = title;
        this.content = content;
        this.address = address;
    }
}
