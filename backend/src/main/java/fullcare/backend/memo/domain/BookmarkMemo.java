package fullcare.backend.memo.domain;

import fullcare.backend.global.entity.BaseEntity;
import fullcare.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BookmarkMemo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmarkmemo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memo_id")
    private Memo memo;

    // ? bookmarkedDate를 따로 만들어서 쓸 것이냐, 아니면 그냥 BaseEntity의 createdDate로 사용할 것이냐?

    @Builder(builderMethodName = "createNewBookmarkMemo")
    public BookmarkMemo(Member member, Memo memo) {
        this.member = member;
        this.memo = memo;
    }
}
