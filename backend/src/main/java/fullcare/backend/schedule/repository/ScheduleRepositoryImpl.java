package fullcare.backend.schedule.repository;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fullcare.backend.evaluation.domain.QMidtermEvaluation;
import fullcare.backend.schedule.ScheduleCategory;
import fullcare.backend.schedule.ScheduleCondition;
import fullcare.backend.schedule.domain.QSchedule;
import fullcare.backend.schedule.domain.Schedule;
import fullcare.backend.util.CustomPageImpl;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fullcare.backend.evaluation.domain.QMidtermEvaluation.midtermEvaluation;
import static fullcare.backend.project.domain.QProject.project;
import static fullcare.backend.schedule.domain.QSchedule.schedule;
import static fullcare.backend.schedulemember.domain.QScheduleMember.scheduleMember;

import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public ScheduleRepositoryImpl(EntityManager em) {
        this.em = em;
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Schedule> search(ScheduleCondition scheduleCondition,Long projectId) {
        List<Schedule> content = jpaQueryFactory.selectFrom(schedule)
                .leftJoin(schedule.project, project)
                .leftJoin(schedule.scheduleMembers, scheduleMember)
                .leftJoin(schedule.midtermEvaluations, midtermEvaluation)
                .where(schedule.project.id.eq(projectId),
                        memberIdEq(scheduleCondition.getMemberId()),
                        scheduleCategoryEq(scheduleCondition.getScheduleCategory()))
                .fetch();
        return content;
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return isEmpty(memberId) ? null : scheduleMember.projectMember.member.id.eq(memberId);
    }
    private BooleanExpression scheduleCategoryEq(ScheduleCategory scheduleCategory) {
        return isEmpty(scheduleCategory) ? null : schedule.dtype.eq(scheduleCategory.toString());
    }

}
