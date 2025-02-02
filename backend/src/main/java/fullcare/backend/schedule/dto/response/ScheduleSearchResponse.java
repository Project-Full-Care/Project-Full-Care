package fullcare.backend.schedule.dto.response;

import fullcare.backend.global.State;
import fullcare.backend.member.domain.Member;
import fullcare.backend.schedule.ScheduleCategory;
import fullcare.backend.schedule.dto.ScheduleMemberDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleSearchResponse {
    private Long scheduleId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ScheduleCategory scheduleCategory;
    private List<ScheduleMemberDto> members = new ArrayList<>();
    private State state;
    private LocalDate modifyDate;
    private boolean check;
    private boolean evaluationRequired;
    @Builder
    public ScheduleSearchResponse(Long scheduleId, String title, LocalDateTime startDate, LocalDateTime endDate, ScheduleCategory scheduleCategory, LocalDate modifyDate , State state) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = state;
        this.modifyDate = modifyDate;
        this.scheduleCategory = scheduleCategory;
    }

    public void addMember(Member member){
        members.add(ScheduleMemberDto.builder()
                .id(member.getId())
                .imageUrl(member.getImageUrl())
                .name(member.getName()).build());
    }
    public void updateCheck(boolean check){
        this.check = check;
    }
    public boolean getEvaluationRequired(){
        return this.evaluationRequired;
    }
}
