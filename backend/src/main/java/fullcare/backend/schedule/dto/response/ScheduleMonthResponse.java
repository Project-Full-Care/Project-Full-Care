package fullcare.backend.schedule.dto.response;

import fullcare.backend.member.domain.Member;
import fullcare.backend.schedule.ScheduleCategory;
import fullcare.backend.schedule.dto.MemberDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
public class ScheduleMonthResponse {
    private Long scheduleId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String address;
    private ScheduleCategory scheduleCategory;
    private List<MemberDto> members = new ArrayList<>();
    @Builder
    public ScheduleMonthResponse(Long scheduleId, String title, LocalDateTime startDate, LocalDateTime endDate, ScheduleCategory scheduleCategory, String address) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.address = address;
        this.scheduleCategory = scheduleCategory;
    }

    public void addMember(Member member){
        members.add(MemberDto.builder()
                .id(member.getId())
                .imageUrl(member.getImageUrl())
                .name(member.getName()).build());
    }

}
