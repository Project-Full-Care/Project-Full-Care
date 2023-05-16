package fullcare.backend.schedule.service;

import fullcare.backend.member.domain.Member;
import fullcare.backend.member.repository.MemberRepository;
import fullcare.backend.project.domain.Project;
import fullcare.backend.project.repository.ProjectRepository;
import fullcare.backend.projectmember.domain.ProjectMember;
import fullcare.backend.projectmember.repository.ProjectMemberRepository;
import fullcare.backend.schedule.ScheduleCategory;
import fullcare.backend.schedule.domain.Meeting;
import fullcare.backend.schedule.domain.Milestone;
import fullcare.backend.schedule.domain.Schedule;
import fullcare.backend.schedule.dto.*;
import fullcare.backend.schedule.dto.request.*;
import fullcare.backend.schedule.dto.response.*;
import fullcare.backend.schedule.repository.ScheduleRepository;
import fullcare.backend.schedulemember.domain.ScheduleMember;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    @Transactional(readOnly = true)
    public List<ScheduleServingResponse> findProjectMembers(Long projectId){
//        Project project = projectRepository.findById(projectId).orElseThrow();
//        List<ScheduleServingResponse> response = project.getProjectMembers().stream().map(pms -> ScheduleServingResponse.builder()
//                .id(pms.getMember().getId())
//                .name(pms.getMember().getName()).build()
//        ).collect(Collectors.toList());

        List<ProjectMember> pmList = projectMemberRepository.findByProjectId(projectId);
        if (pmList.size()==0){
            throw new RuntimeException("프로젝트 조회 불가");
        }
        List<ScheduleServingResponse> response = pmList.stream().map(pms -> ScheduleServingResponse.builder()
                .id(pms.getMember().getId())
                .name(pms.getMember().getName()).build()).collect(Collectors.toList());
        return response;
    }
    public boolean updateSchedule(ScheduleUpdateRequest scheduleUpdateRequest, Long scheduleId) {// 멤버 로그인 사용자 검증 수정
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        List<ProjectMember> pmList = projectMemberRepository.findByProjectId(scheduleUpdateRequest.getProjectId());
        List<Member> members = pmList.stream().map(pm -> pm.getMember()).collect(Collectors.toList());

        schedule.update(
                scheduleUpdateRequest.getState(),
                scheduleUpdateRequest.getTitle(),
                scheduleUpdateRequest.getContent(),
                scheduleUpdateRequest.getStartDate(),
                scheduleUpdateRequest.getEndDate()
        );
        List<Member> updateMemberList = memberRepository.findByIds(scheduleUpdateRequest.getMemberIds()); // 새로 업데이트 되는 멤버 리스트
        if (!members.containsAll(updateMemberList)){// 프로젝트에 속한 사람인지 확인
            return false;}

        schedule.getScheduleMembers().clear(); // 스케쥴 멤버가 삭제 되는 경우에 대한 방안이 없음
        for (Member member : updateMemberList) {
            schedule.addMember(member);
        }

        if (scheduleUpdateRequest.getCategory().equals(ScheduleCategory.미팅)){ // Meeting
            Meeting meeting = (Meeting)schedule;
            meeting.updateAddress(scheduleUpdateRequest.getAddress());
        }else{ // Milestone
        }
        return true;
    }
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        scheduleRepository.delete(schedule);
    }
    @Transactional(readOnly = true)
    public CustomResponseDto<ScheduleListResponse> findScheduleList(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();

        //schedule로 통합 필요
        List<Schedule> meetingList = scheduleRepository.findMeetingByProjectId(projectId); // dtype을 구분 짓기 위해 각 메소드를 만듬
        List<ScheduleListResponse> scheduleListResponseList = toListResponse(meetingList, ScheduleCategory.미팅);

        List<Schedule> milestoneList = scheduleRepository.findMileStoneByProjectId(projectId);
        List<ScheduleListResponse> response2 = toListResponse(milestoneList,ScheduleCategory.개발일정);
        /////////////////
        scheduleListResponseList.addAll(response2);

        scheduleListResponseList.sort(Comparator.comparing(ScheduleListResponse::getStartDate));// 날짜 기준 내림차순 정렬

        CustomResponseDto<ScheduleListResponse> response = new CustomResponseDto<>(startDate, endDate, scheduleListResponseList);

        return response;
    }

    @Transactional(readOnly = true)
    public ScheduleCalenderMonthResponse findScheduleCalenderList(ScheduleMonthRequest scheduleMonthRequest) {
        LocalDate localDate = LocalDateTime.now().toLocalDate();
        int lastDay = LocalDateTime.now().toLocalDate().withDayOfMonth(localDate.lengthOfMonth()).getDayOfMonth();
        LocalDateTime startDate = LocalDateTime.of(scheduleMonthRequest.getYear(),scheduleMonthRequest.getMonth(), 1, 0, 0,0);
        LocalDateTime endDate = LocalDateTime.of(scheduleMonthRequest.getYear(), scheduleMonthRequest.getMonth(),lastDay,23,59,59  );

        List<Schedule> scheduleList = scheduleRepository.findByStartDateBetweenOrEndDateBetween( startDate, endDate,startDate, endDate  );

        ScheduleCalenderMonthResponse scheduleMonthResponse = toScheduleMonthResponse(scheduleList);
        scheduleMonthResponse.getMeetings().sort(Comparator.comparing(MeetingDto::getStartDate));// 날짜 기준 내림차순 정렬
        scheduleMonthResponse.getMilestones().sort(Comparator.comparing(MilestoneDto::getStartDate));// 날짜 기준 내림차순 정렬
        return scheduleMonthResponse;
    }
    @Transactional(readOnly = true)
    public Page<ScheduleMonthResponse> findScheduleMonthList(Pageable pageable, ScheduleMonthRequest scheduleMonthRequest, Member member) {
        Member findMember = memberRepository.findById(member.getId()).orElseThrow();

        LocalDate localDate = LocalDateTime.now().toLocalDate();
        int lastDay = LocalDateTime.now().toLocalDate().withDayOfMonth(localDate.lengthOfMonth()).getDayOfMonth();
        LocalDateTime startDate = LocalDateTime.of(scheduleMonthRequest.getYear(),scheduleMonthRequest.getMonth(), 1, 0, 0,0);
        LocalDateTime endDate = LocalDateTime.of(scheduleMonthRequest.getYear(), scheduleMonthRequest.getMonth(),lastDay,23,59,59  );
        Page<Schedule> pageSchedule = scheduleRepository.findMonthByStartDateBetweenOrEndDateBetween(pageable, startDate, endDate, startDate, endDate);
        List<ScheduleMonthResponse> scheduleMonthResponses = new ArrayList<>();
        addResponse(pageSchedule, scheduleMonthResponses, findMember);// 미팅, 마일스톤에 맞게 일정 생성 후 응답에 넣기

        return new PageImpl<>(scheduleMonthResponses, pageable, scheduleMonthResponses.size());
    }
//    @Transactional(readOnly = true)
//    public Page<ScheduleMonthResponse> findScheduleMemberMonthList(Pageable pageable, ScheduleMonthListRequest scheduleMonthListRequest) {
//        LocalDate localDate = LocalDateTime.now().toLocalDate();
//        int lastDay = LocalDateTime.now().toLocalDate().withDayOfMonth(localDate.lengthOfMonth()).getDayOfMonth();
//        LocalDateTime startDate = LocalDateTime.of(scheduleMonthListRequest.getYear(),scheduleMonthListRequest.getMonth(), 1, 0, 0,0);
//        LocalDateTime endDate = LocalDateTime.of(scheduleMonthListRequest.getYear(), scheduleMonthListRequest.getMonth(),lastDay,23,59,59  );
//        Page<Schedule> pageSchedule = scheduleRepository.findMonthByStartDateBetweenOrEndDateBetween(pageable, startDate, endDate, startDate, endDate);
//        return null;
//    }
    public void updateState(ScheduleStateUpdateRequest scheduleStateUpdateRequest, Long memberId){
        Schedule schedule = scheduleRepository.findById(memberId).orElseThrow();
        schedule.updateState(scheduleStateUpdateRequest.getState());

    }
    @Transactional(readOnly = true) // 프로젝트별 일정 내용
    public ScheduleMyListResponse findMySchedule(ScheduleMonthRequest scheduleMonthRequest, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        LocalDate localDate = LocalDateTime.now().toLocalDate();
        int lastDay = LocalDateTime.now().toLocalDate().withDayOfMonth(localDate.lengthOfMonth()).getDayOfMonth();
        LocalDateTime startDate = LocalDateTime.of(scheduleMonthRequest.getYear(),scheduleMonthRequest.getMonth(), 1, 0, 0,0);
        LocalDateTime endDate = LocalDateTime.of(scheduleMonthRequest.getYear(), scheduleMonthRequest.getMonth(),lastDay,23,59,59  );
        List<Schedule> scheduleList = scheduleRepository.findMonthListByMember( memberId, startDate, endDate);
        List<ScheduleMyDto> scheduleDtos = new ArrayList<>();
        Schedule nearSchedule = toMySchedule(member, scheduleList, scheduleDtos);// 일정 리스트를 설정 및 가장 가까운 일정 반환

        ScheduleMyListResponse scheduleMyListResponse = new ScheduleMyListResponse();
        scheduleMyListResponse.setScheduleMyDtos(scheduleDtos);

        ScheduleDto scheduleDto = ScheduleDto.builder()// 가장 가까운 일정을 응답값으로 변환
                .projectId(nearSchedule.getProject().getId())
                .scheduleId(nearSchedule.getId())
                .title(nearSchedule.getTitle())
                .content(nearSchedule.getContent())
                .startDate(nearSchedule.getStartDate())
                .endDate(nearSchedule.getEndDate()).build();
        if(nearSchedule instanceof Meeting){
            scheduleDto.setAddress(((Meeting) nearSchedule).getAddress());
        }
        scheduleMyListResponse.setNearSchedule(scheduleDto);

        return scheduleMyListResponse;
    }

    private Schedule toMySchedule(Member member, List<Schedule> scheduleList, List<ScheduleMyDto> scheduleDtos) {
        ScheduleMyDto scheduleMyDto = null;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nearDate = now.plusMonths(1l);
        Schedule nearSchedule = scheduleList.get(0);
        for (Schedule schedule : scheduleList) {
            if(now.isAfter(schedule.getStartDate()) && nearDate.isBefore(schedule.getStartDate())){
                nearDate = schedule.getStartDate();
                nearSchedule = schedule;
            }
            scheduleMyDto = ScheduleMyDto.builder()
                    .projectId(schedule.getProject().getId())
                    .scheduleId(schedule.getId())
                    .title(schedule.getTitle())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .build();

            Set<ScheduleMember> scheduleMembers = schedule.getScheduleMembers();
            for (ScheduleMember scheduleMember : scheduleMembers) {
                if(scheduleMember.getMember() == member && scheduleMember.getRecentView().isBefore(schedule.getModifiedDate())){
                    System.out.println("확인 못함" );
                    scheduleMyDto.updateCheck(false);
                }else if (scheduleMember.getMember() == member){
                    System.out.println("확인 함" );
                    scheduleMyDto.updateCheck(true);
                }else{
                    System.out.println("로그인한 멤버 정보가 아님");
                }
            }
            if (schedule instanceof Meeting){
                Meeting meeting = (Meeting) schedule;
                scheduleMyDto.setAddress(meeting.getAddress());
                scheduleMyDto.setScheduleCategory(ScheduleCategory.미팅);
            }else{
                scheduleMyDto.setScheduleCategory(ScheduleCategory.개발일정);
            }
            scheduleDtos.add(scheduleMyDto);
        }
        return nearSchedule;
    }

    private ScheduleCalenderMonthResponse toScheduleMonthResponse(List<Schedule> scheduleList) {
        ScheduleCalenderMonthResponse scheduleMonthResponse = new ScheduleCalenderMonthResponse();

        for (Schedule schedule : scheduleList) {
            if (schedule instanceof Meeting){
                Meeting meeting = (Meeting) schedule;
                if (meeting.getAddress() != null){
                    MeetingDto meetingDto = MeetingDto.builder()
                            .scheduleId(schedule.getId())
                            .title(schedule.getTitle())
                            .content(meeting.getContent())
                            .startDate(meeting.getStartDate())
                            .endDate(meeting.getEndDate())
                            .address(meeting.getAddress()).build();
                    Set<ScheduleMember> scheduleMembers = schedule.getScheduleMembers();
                    scheduleMembers.forEach(sm -> {
                        meetingDto.addMember(sm.getMember());
                    });
                    scheduleMonthResponse.addMeeting(meetingDto);
                }
            }else{
                Milestone milestone = (Milestone) schedule;
                MilestoneDto milestoneDto = MilestoneDto.builder()
                        .scheduleId(milestone.getId())
                        .title(milestone.getTitle())
                        .content(milestone.getContent())
                        .startDate(milestone.getStartDate())
                        .endDate(milestone.getEndDate())
                        .build();
                Set<ScheduleMember> scheduleMembers = milestone.getScheduleMembers();
                scheduleMembers.forEach(sm -> {
                    milestoneDto.addMember(sm.getMember());
                });
                scheduleMonthResponse.addMilestone(milestoneDto);
            }
        }
        return scheduleMonthResponse;
    }


    private List<ScheduleListResponse> toListResponse(List<Schedule> scheduleList, ScheduleCategory scheduleCategory) {
        return scheduleList.stream().map(s -> ScheduleListResponse.builder()
                .scheduleId(s.getId())
                .title(s.getTitle())
                .content(s.getContent())
                .startDate(s.getStartDate())
                .category(scheduleCategory.name())
                .build()
        ).collect(Collectors.toList());
    }


    private void addResponse(Page<Schedule> pageSchedule, List<ScheduleMonthResponse> scheduleMonthResponses, Member member) {
        for (Schedule schedule : pageSchedule) {
            ScheduleMonthResponse scheduleResponse = ScheduleMonthResponse.builder()
                    .scheduleId(schedule.getId())
                    .title(schedule.getTitle())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate()).build();

            checkModify(member, schedule, scheduleResponse);

            if (schedule instanceof Meeting){
                scheduleResponse.setAddress(scheduleResponse.getAddress());
                scheduleResponse.setScheduleCategory(ScheduleCategory.미팅);
            }else{
                scheduleResponse.setScheduleCategory(ScheduleCategory.개발일정);
            }
            scheduleMonthResponses.add(scheduleResponse);

        }
    }

    private void checkModify(Member member, Schedule schedule, ScheduleMonthResponse scheduleResponse) {
        Set<ScheduleMember> scheduleMembers = schedule.getScheduleMembers();
        scheduleMembers.forEach(sm -> {
            scheduleResponse.addMember(sm.getMember());
                if(sm.getMember() == member && sm.getRecentView().isBefore(schedule.getModifiedDate())){
                    System.out.println("확인 못함" );
                    scheduleResponse.updateCheck(false);
                }else if (sm.getMember() == member){
                    System.out.println("확인 함" );
                    scheduleResponse.updateCheck(true);
                }else{
                    System.out.println("로그인한 멤버 정보가 아님");
                }
            });
    }

    @Transactional
    public ScheduleDetailResponse findSchedule(Long projectId, Long scheduleId, Long memberId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        List<ProjectMember> projectMembers = project.getProjectMembers();
        Set<ScheduleMember> scheduleMembers = schedule.getScheduleMembers();

        ScheduleDetailResponse scheduleDetailResponse = ScheduleDetailResponse.builder()
                .projectId(projectId)
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .build();
        for (ProjectMember projectMember : projectMembers) {
            DetailMemberDto detailMemberDto = DetailMemberDto.builder().id(projectMember.getMember().getId())
                    .name(projectMember.getMember().getName()).build();
            for (ScheduleMember scheduleMember : scheduleMembers) {
                if (projectMember.getMember() == scheduleMember.getMember()){
                    detailMemberDto.setIn(true);
                }
                if(projectMember.getMember() == scheduleMember.getMember() && projectMember.getMember().getId() == memberId){ // 로그인한 사용자 최근 확인한 일정 갱신
                    scheduleMember.updateRecentView();
                }
            }
            scheduleDetailResponse.addMember(detailMemberDto);
        }

        if(schedule instanceof Meeting){
            scheduleDetailResponse.setScheduleCategory(ScheduleCategory.미팅);
            scheduleDetailResponse.setAddress(scheduleDetailResponse.getAddress());
        }else{
            scheduleDetailResponse.setScheduleCategory(ScheduleCategory.개발일정);
        }



        return scheduleDetailResponse;

    }

}
