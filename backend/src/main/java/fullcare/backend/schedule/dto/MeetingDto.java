package fullcare.backend.schedule.dto;

import fullcare.backend.member.domain.Member;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class MeetingDto {
    private Long scheduleId;
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String address;
    private List<ScheduleMemberDto> members = new ArrayList<>();
    @Builder
    public MeetingDto(Long scheduleId, String title, String content, LocalDateTime startDate, LocalDateTime endDate, String address) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.address = address;
    }
    public void addMember(Member member){
        members.add(ScheduleMemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .imageUrl(member.getImageUrl()).build());
    }
}
