package fullcare.backend.schedule.service;

import fullcare.backend.global.State;
import fullcare.backend.global.errorcode.MemberErrorCode;
import fullcare.backend.global.errorcode.ProjectErrorCode;
import fullcare.backend.global.exceptionhandling.exception.CompletedProjectException;
import fullcare.backend.global.exceptionhandling.exception.EntityNotFoundException;
import fullcare.backend.member.domain.Member;
import fullcare.backend.member.repository.MemberRepository;
import fullcare.backend.project.domain.Project;
import fullcare.backend.project.repository.ProjectRepository;
import fullcare.backend.project.service.ProjectService;
import fullcare.backend.projectmember.domain.ProjectMember;
import fullcare.backend.projectmember.repository.ProjectMemberRepository;
import fullcare.backend.schedule.domain.Milestone;
import fullcare.backend.schedule.domain.Schedule;
import fullcare.backend.schedule.dto.request.ScheduleCreateRequest;
import fullcare.backend.schedule.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static fullcare.backend.global.errorcode.ScheduleErrorCode.INVALID_CREATE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MilestoneService {
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectService projectService;

    public void createMilestone(ScheduleCreateRequest scheduleCreateRequest, Long memberId) {
        try {
            ProjectMember projectMember = projectService.isProjectAvailable(scheduleCreateRequest.getProjectId(), memberId, false);
            LocalDateTime now = LocalDateTime.now();
            Project project = projectMember.getProject();
            LocalDateTime startDate = project.getStartDate().atStartOfDay();
            LocalDateTime endDate = project.getEndDate().atStartOfDay();
            Schedule.validDate(startDate, endDate, scheduleCreateRequest.getStartDate(), scheduleCreateRequest.getEndDate());

            List<Long> memberIds = scheduleCreateRequest.getMemberIds();
            List<ProjectMember> memberList = new ArrayList<>();
            memberIds.forEach(id -> {
                ProjectMember pm = projectMemberRepository.findByProjectIdAndMemberId(scheduleCreateRequest.getProjectId(), id).orElseThrow(() -> new EntityNotFoundException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
                memberList.add(pm);
            });
            Milestone milestone = Milestone.builder()
                    .project(project)
                    .startDate(scheduleCreateRequest.getStartDate())
                    .endDate(scheduleCreateRequest.getEndDate())
                    .title(scheduleCreateRequest.getTitle())
                    .content(scheduleCreateRequest.getContent())
                    .author(projectMember)
                    .createdDate(now)
                    .modifiedDate(now)
                    .state(State.TBD)
                    .build();
            milestone.addMemberList(memberList);

            milestoneRepository.save(milestone);
        } catch (CompletedProjectException completedProjectException) {
            throw new CompletedProjectException(INVALID_CREATE);
        }

    }
}
