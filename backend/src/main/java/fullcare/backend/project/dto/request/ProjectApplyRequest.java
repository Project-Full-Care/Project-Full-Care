package fullcare.backend.project.dto.request;

import fullcare.backend.projectmember.domain.ProjectMemberPositionType;
import lombok.Getter;

@Getter
public class ProjectApplyRequest {

    private ProjectMemberPositionType position;
}