package fullcare.backend.post.dto.response;

import fullcare.backend.post.domain.Post;
import fullcare.backend.post.domain.Recruitment;
import fullcare.backend.post.dto.request.RecruitInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@Getter
public class PostListResponse {

    private Long postId;
    private String projectName; // * 프로젝트 이름


    private String title;
    private String techStack;
    private int isLiked;


    private List<RecruitInfo> recruitInfoList;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public PostListResponse(Post post) {//, int isLiked) {
        this.postId = post.getId();
        this.projectName = post.getProjectMember().getProject().getTitle();
        this.title = post.getTitle();
        this.techStack = post.getTechStack();
//        this.isLiked = isLiked;
        this.recruitInfoList = post.getRecruitments().stream().map(r -> new RecruitInfo(r)).toList();
        this.createdDate = post.getCreatedDate();
        this.modifiedDate = post.getModifiedDate();
    }

    @Builder
    public PostListResponse(Long postId, String projectName, String title, String techStack,
                            int isLiked, List<Recruitment> recruitments,
                            LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.postId = postId;
        this.projectName = projectName;
        this.title = title;
        this.techStack = techStack;
        this.isLiked = isLiked;
        this.recruitInfoList = recruitments.stream().map(r -> new RecruitInfo(r)).toList();
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;

    }

    public static PostListResponse entityToDto(Post post) {
        return PostListResponse.builder()
                .postId(post.getId())
                .projectName(post.getProjectMember().getProject().getTitle())
                .title(post.getTitle())
                .techStack(post.getTechStack())
//                .recruitInfoList(post.getRecruitments().stream().map(r -> new RecruitInfo(r)).toList())
                .createdDate(post.getCreatedDate())
                .modifiedDate(post.getModifiedDate())
                .build();
    }

}