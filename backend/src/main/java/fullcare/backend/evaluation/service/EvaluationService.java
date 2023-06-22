package fullcare.backend.evaluation.service;

import fullcare.backend.evaluation.dao.BadgeDao;
import fullcare.backend.evaluation.dao.ScoreDao;
import fullcare.backend.evaluation.domain.EvaluationBadge;
import fullcare.backend.evaluation.domain.FinalTermEvaluation;
import fullcare.backend.evaluation.domain.MidtermEvaluation;
import fullcare.backend.evaluation.domain.Score;
import fullcare.backend.evaluation.dto.*;
import fullcare.backend.evaluation.dto.request.FinalEvalCreateRequest;
import fullcare.backend.evaluation.dto.request.FinalEvalUpdateRequest;
import fullcare.backend.evaluation.dto.request.MidTermEvalCreateRequest;
import fullcare.backend.evaluation.dto.response.*;
import fullcare.backend.evaluation.dto.ChartDto;
import fullcare.backend.evaluation.exceptionhandler.exception.*;
import fullcare.backend.evaluation.repository.FinalEvaluationRepository;
import fullcare.backend.evaluation.repository.MidtermEvaluationRepository;
import fullcare.backend.global.State;
import fullcare.backend.global.exception.InvalidAccessException;
import fullcare.backend.member.domain.Member;
import fullcare.backend.member.repository.MemberRepository;
import fullcare.backend.project.domain.Project;
import fullcare.backend.project.repository.ProjectRepository;
import fullcare.backend.projectmember.domain.ProjectMember;
import fullcare.backend.projectmember.repository.ProjectMemberRepository;
import fullcare.backend.schedule.domain.Schedule;
import fullcare.backend.schedule.repository.ScheduleRepository;
import fullcare.backend.schedulemember.repository.ScheduleMemberRepository;
import fullcare.backend.util.CustomPageImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EvaluationService {

    private final FinalEvaluationRepository finalEvaluationRepository;
    private final MidtermEvaluationRepository midtermEvaluationRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ScheduleRepository scheduleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ScheduleMemberRepository scheduleMemberRepository;
    public boolean validateFinalDuplicationAuthor(Long evaluatedId, Long authorId, Long projectId){
        return finalEvaluationRepository.existsByEvaluatedIdAndEvaluatorIdAndProjectId(evaluatedId, authorId, projectId);
    }
    public boolean validateAuthor(Long evaluationId, Long authorId){
        return finalEvaluationRepository.existsByIdAndEvaluatorId(evaluationId, authorId);
    }
    public boolean validateMidDuplicationAuthor(Long scheduleId, Long voterId){
        return midtermEvaluationRepository.existsByScheduleIdAndVoterId(scheduleId, voterId);
    }


    @Transactional
    public void createMidtermEvaluation(MidTermEvalCreateRequest midTermEvalCreateRequest, Member voter) {
        Member voted = memberRepository.findById(midTermEvalCreateRequest.getVotedId()).orElseThrow(() -> new EntityNotFoundException("해당 사용자가 없습니다."));
        if(voter.getId() == voted.getId()){
            throw new MyEvalException("자신의 평가는 불가능합니다.");
        }
        if(validateMidDuplicationAuthor(midTermEvalCreateRequest.getScheduleId(), voter.getId())){
            throw new EvalDuplicateException("중복 평가는 불가능합니다.");
        }
        scheduleMemberRepository.findByScheduleIdAndMemberId(midTermEvalCreateRequest.getScheduleId(), voter.getId()).orElseThrow(() ->  new EntityNotFoundException("일정에 해당 사용자가 없습니다."));//* 일정에 투표하는 사람이 없을때
        scheduleMemberRepository.findByScheduleIdAndMemberId(midTermEvalCreateRequest.getScheduleId(), voted.getId()).orElseThrow(() ->  new EntityNotFoundException("일정에 해당 사용자가 없습니다."));//* 일정에 투표된 사람이 없을때
        Schedule schedule = scheduleRepository.findByIdAndState(midTermEvalCreateRequest.getScheduleId(), State.COMPLETE).orElseThrow(() -> new InvalidAccessException("일정이 완료 안됐습니다."));//* 일정이 완료 됐을때만 가능
        Project project = projectRepository.findById(midTermEvalCreateRequest.getProjectId()).orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        MidtermEvaluation newMidtermEvaluation = MidtermEvaluation.createNewMidtermEval()
                .evaluationBadge(midTermEvalCreateRequest.getEvaluationBadge())
                .voter(voter)
                .voted(voted)
                .project(project)
                .schedule(schedule)
                .build();

        midtermEvaluationRepository.save(newMidtermEvaluation);
    }

    @Transactional
    public void createFinalEvaluation(FinalEvalCreateRequest finalEvalCreateRequest, Member evaluator) {
        Member evaluated = memberRepository.findById(finalEvalCreateRequest.getEvaluatedId()).orElseThrow(() -> new EntityNotFoundException("해당 사용자가 없습니다."));
        Project project = projectRepository.findById(finalEvalCreateRequest.getProjectId()).orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 없습니다."));
        projectMemberRepository.findByProjectIdAndMemberId(project.getId(), finalEvalCreateRequest.getEvaluatedId()).orElseThrow(() -> new EntityNotFoundException("투표된 사람은 프로젝트에 없습니다."));
        //? 점수 5점 이상일 경우 에러처리
        if (!Score.valid(finalEvalCreateRequest.getScore())){
            throw new EvalOutOfRangeException("평가 점수 범위가 벗어났습니다.");
        }
        if(evaluator.getId() == evaluated.getId()){
            throw new MyEvalException("자신의 평가는 불가능합니다.");
        }
        if(!project.getState().equals(State.COMPLETE)){
            throw new EvalNotCompleteProjectException("프로젝트가 완료되지 않은 평가는 불가능합니다.");
        }
        if ((validateFinalDuplicationAuthor(finalEvalCreateRequest.getEvaluatedId(), evaluator.getId(), finalEvalCreateRequest.getProjectId()))) {
            throw new EvalDuplicateException("중복 평가는 불가능합니다.");
        }
        FinalTermEvaluation newFinalTermEvaluation = FinalTermEvaluation.createNewFinalEval()
                .project(project)
                .content(finalEvalCreateRequest.getContent())
                .score(finalEvalCreateRequest.getScore())
                .evaluator(evaluator)
                .evaluated(evaluated)
                .state(finalEvalCreateRequest.getState())
                .build();
        finalEvaluationRepository.save(newFinalTermEvaluation);
    }

    @Transactional //* 임시 저장한 평가를 수정 또는 완료할 때 사용
    public void updateFinalEvaluation(Long evaluationId, FinalEvalUpdateRequest finalEvalUpdateRequest) {
        if (finalEvaluationRepository.existsByIdAndState(evaluationId, State.COMPLETE)){
            throw new CompletedEvalException("완료된 평가는 수정이 안됩니다.");
        }
        if (!Score.valid(finalEvalUpdateRequest.getScore())){
            throw new EvalOutOfRangeException("평가 점수 범위가 벗어났습니다.");
        }
        FinalTermEvaluation finalTermEvaluation = finalEvaluationRepository.findById(evaluationId).orElseThrow(() -> new EntityNotFoundException("해당 최종 평가가 존재하지 않습니다."));
        finalTermEvaluation.update(finalEvalUpdateRequest);
    }

    public List<BadgeDto> findMidtermEvaluationDetailResponse(Long projectId, Long memberId) {
        List<BadgeDto> midtermDetailResponses = midtermEvaluationRepository.findAllByMemberId(projectId, memberId);
        setBadge(midtermDetailResponses);
        return  midtermDetailResponses;
    }
    @Transactional
    public void deleteFinalEvaluation(Long evaluationId, Long projectId) {
        if (finalEvaluationRepository.existsByIdAndState(evaluationId, State.COMPLETE)){
            throw new CompletedEvalException("완료된 평가는 삭제가 안됩니다.");
        }
        FinalTermEvaluation finalTermEvaluation = finalEvaluationRepository.findById(evaluationId).orElseThrow(() -> new EntityNotFoundException("해당 최종 평가가 존재하지 않습니다."));
        finalEvaluationRepository.delete(finalTermEvaluation);
    }

    public FinalEvaluationResponse findFinalEvaluationDetailResponse(Long evaluationId) {
        FinalTermEvaluation findFinalTermEvaluation = finalEvaluationRepository.findById(evaluationId).orElseThrow(() -> new EntityNotFoundException("해당 최종 평가가 존재하지 않습니다."));
        
        return FinalEvaluationResponse.entityToDto(findFinalTermEvaluation);
    }

    public EverythingEvalResponse findMidtermEvaluationList(Long projectId) {
        Project project = projectRepository.findJoinPMJoinMemberById(projectId).orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        List<Member> members = project.getProjectMembers().stream().map(pm -> pm.getMember()).collect(Collectors.toList());
        List<BadgeDao> midtermBadgeList = midtermEvaluationRepository.findList(projectId, members);
        List<MidTermRankProjectionInterface> rank = midtermEvaluationRepository.findRank(projectId);

        List<ChartDto> midTermEvalChartDto = members.stream().map(m -> ChartDto.builder()
                .memberId(m.getId())
                .name(m.getName())
                .build()
        ).collect(Collectors.toList());
        for (BadgeDao badgeDao : midtermBadgeList) {
            for (ChartDto chart : midTermEvalChartDto) {
                if(badgeDao.getMemberId() == chart.getMemberId()){
                    chart.addEvaluation(new BadgeDto(badgeDao.getEvaluationBadge(), badgeDao.getQuantity()));
                }
            }
        }

        List<MidTermRankingDto> rankingDtos = new ArrayList<>();// 랭킹 부분
        for (MidTermRankProjectionInterface r : rank) {
            String name = members.stream().filter(m -> m.getId() == r.getQuantity()).map(m -> m.getName()).findFirst().get();
            rankingDtos.add(MidTermRankingDto.builder()
                    .rank(r.getRanking())
                    .memberId(r.getId())
                    .name(name)
                    .quantity(r.getQuantity())
                    .build()
            );
        }

        for (ChartDto chartDto : midTermEvalChartDto) {
//            System.out.println("m.getBadges() = " + chartDto.getBadges());
            setBadge(chartDto.getEvaluation());
        }
        EverythingEvalResponse everythingEvalResponse = new EverythingEvalResponse(midTermEvalChartDto, rankingDtos);
        return everythingEvalResponse;
    }

    public EverythingEvalResponse findFinalEvaluationList(Long projectId) {
        Project project = projectRepository.findJoinPMJoinMemberById(projectId).orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        List<Member> members = project.getProjectMembers().stream().map(pm -> pm.getMember()).collect(Collectors.toList());
        List<ScoreDao> scoreDaos = finalEvaluationRepository.findList(projectId, members);

        List<ChartDto> finalTermEvalChartDto = members.stream().map(m -> ChartDto.builder()
                .memberId(m.getId())
                .name(m.getName())
                .build()
        ).collect(Collectors.toList());
        for (ScoreDao s : scoreDaos) {
            for (ChartDto chart : finalTermEvalChartDto) {
                if(s.getId() == chart.getMemberId()){
                    chart.addEvaluation(ScoreDto.builder()
                            .jobPerformance(s.getJobPerformance())
                            .punctuality(s.getPunctuality())
                            .communication(s.getCommunication())
                            .sincerity(s.getSincerity()).build());
                }
            }
        }
        finalTermEvalChartDto.stream().filter(fe->fe.getEvaluation().size()==0).forEach(fe->fe.addEvaluation(new ScoreDto()));

        // * 랭킹 부분
        List<FinalTermRankingDto> rankingDtos = finalTermEvalChartDto.stream().map(fe -> FinalTermRankingDto.builder()
                .memberId(fe.getMemberId())
                .name(fe.getName())
                .score(Score.avg((ScoreDto) fe.getEvaluation().get(0)))
                .build()).collect(Collectors.toList());
        Collections.sort(rankingDtos, (a, b) -> (int)Math.round(b.getScore() - a.getScore()));
        List<FinalTermRankingDto> ranks = rankingDtos.stream().filter(r -> r.getScore() != 0).collect(Collectors.toList());// 0점 랭킹에서 제거

        for (FinalTermRankingDto exRank : rankingDtos) {
            Long rank = 1l;
            for (FinalTermRankingDto inRank : rankingDtos) {
                if(exRank.getScore()<inRank.getScore()){
                    rank++;
                }
            }
            exRank.setRank(rank);
        }

        EverythingEvalResponse everythingEvalResponse = new EverythingEvalResponse(finalTermEvalChartDto, ranks);
        return everythingEvalResponse;
    }

    public MidTermEvalModalResponse modal(Long scheduleId, Long memberId) {
        Schedule schedule = scheduleRepository.findJoinSMJoinMemberById(scheduleId).orElseThrow(() -> new EntityNotFoundException("해당 일정이 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자가 존재하지 않습니다."));
        List<MemberDto> memberDto = schedule.getScheduleMembers().stream().filter(sm -> sm.getMember() != member).map(sm -> MemberDto.builder().member(sm.getMember()).build()).collect(Collectors.toList());// 나를 제외한 일정에 참여한 팀원


        MidTermEvalModalResponse midTermEvalModalResponse = MidTermEvalModalResponse.builder()
                .title(schedule.getTitle())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .state(schedule.getState())
                .build();
        midTermEvalModalResponse.setMembers(memberDto);
        List<BadgeDto> badgeDtoDtos = new ArrayList<>();
        setBadge(badgeDtoDtos);
        midTermEvalModalResponse.setBadgeDtos(badgeDtoDtos);
        return midTermEvalModalResponse;
    }
    public List<ParticipantResponse> findParticipantList(Long projectId, Long memberId){
        Project project = projectRepository.findJoinPMJoinMemberById(projectId).orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        List<Member> members = project.getProjectMembers().stream().map(pm -> pm.getMember()).collect(Collectors.toList());
        List<BadgeDao> midtermBadgeList = midtermEvaluationRepository.findList(projectId, members);
        List<FinalTermEvaluation> finalEvalList = new ArrayList<>();
        if(project.getState().equals(State.COMPLETE)){
            finalEvalList = finalEvaluationRepository.findByProjectIdAndEvaluatorId(projectId, memberId);
        }
        List<ParticipantResponse> response = new ArrayList<>();
        for (Member member : members) {
            ParticipantResponse participantResponse = ParticipantResponse.builder().
                    id(member.getId())
                    .name(member.getName())
                    .imageUrl(member.getImageUrl())
                    .build();
            for (BadgeDao badgeDao : midtermBadgeList) {
                if(member.getId() == badgeDao.getMemberId()){
                    BadgeDto badgeDto = new BadgeDto(badgeDao.getEvaluationBadge(), badgeDao.getQuantity());
                    participantResponse.addBadge(badgeDto);
                }
            }
            for (FinalTermEvaluation fe : finalEvalList) { // * 로그인한 사용자가 다른사람 최종평가를 작성한적이 있으면 최종평가 ID 추가 없으면 null
                if(project.getState().equals(State.COMPLETE) && fe.getEvaluated() == member){
                    participantResponse.setFinalEvalId(fe.getId());
                }
            }

            response.add(participantResponse);
        }

        return response;
    }



    private static void setBadge(List<BadgeDto> badgeDtoDtos) {
        List<EvaluationBadge> evaluationBadges = new ArrayList<>();
        evaluationBadges.add(EvaluationBadge.탁월한_리더);
        evaluationBadges.add(EvaluationBadge.열정적인_참여자);
        evaluationBadges.add(EvaluationBadge.최고의_서포터);
        evaluationBadges.add(EvaluationBadge.아이디어_뱅크);
        for (BadgeDto badgeDto : badgeDtoDtos) {
            if(badgeDto.getEvaluationBadge().equals(EvaluationBadge.탁월한_리더)){
                evaluationBadges.remove(EvaluationBadge.탁월한_리더);
            }else if(badgeDto.getEvaluationBadge().equals(EvaluationBadge.열정적인_참여자)){
                evaluationBadges.remove(EvaluationBadge.열정적인_참여자);
            }else if(badgeDto.getEvaluationBadge().equals(EvaluationBadge.최고의_서포터)){
                evaluationBadges.remove(EvaluationBadge.최고의_서포터);
            }else if(badgeDto.getEvaluationBadge().equals(EvaluationBadge.아이디어_뱅크)){
                evaluationBadges.remove(EvaluationBadge.아이디어_뱅크);
            }
        }
        for (EvaluationBadge evaluationBadge : evaluationBadges) { // 없는 뱃지 추가
            badgeDtoDtos.add(new BadgeDto(evaluationBadge,0l));
        }
    }
////////////////////////////////////////////////////////////////////////////////
    // * 개인 페이지 서비스
    public Page<MyEvalListResponse> findMyEvalList(Pageable pageable, Long memberId) {
        Page<ProjectMember> pmList = projectMemberRepository.findByMemberId(pageable, memberId);
        List<MyEvalListResponse> myEvalListResponseList = new ArrayList<>();
        List<Long> projectIds = pmList.stream().map(pm -> pm.getProject().getId()).collect(Collectors.toList());

        List<FinalTermEvaluation> myFinalEvalList = finalEvaluationRepository.findByProjectIdsAndEvaluatedIdAndState(projectIds, memberId, State.COMPLETE);
        int feCnt = 0;
        for (ProjectMember pm : pmList) {
            feCnt = 0;
            ScoreDto scoreDto = new ScoreDto();
            for (FinalTermEvaluation fe : myFinalEvalList){
                if(fe.getProject().getId() == pm.getProject().getId()) {
                    feCnt++;
                    scoreDto.setCommunication(scoreDto.getCommunication()+fe.getScore().getCommunication());
                    scoreDto.setPunctuality(scoreDto.getPunctuality()+fe.getScore().getPunctuality());
                    scoreDto.setSincerity(scoreDto.getSincerity()+fe.getScore().getSincerity());
                    scoreDto.setJobPerformance(scoreDto.getJobPerformance()+fe.getScore().getJobPerformance());
                }
            }
//            scoreDto.setJobPerformance(scoreDto.getJobPerformance()/ feCnt);
//            scoreDto.setPunctuality(scoreDto.getPunctuality()/ feCnt);
//            scoreDto.setCommunication(scoreDto.getCommunication()/ feCnt);
//            scoreDto.setSincerity(scoreDto.getSincerity()/ feCnt);

            scoreDto.setSincerity(Math.round(scoreDto.getSincerity()/ feCnt*100)/100.0);
            scoreDto.setJobPerformance(Math.round(scoreDto.getJobPerformance()/ feCnt*100)/100.0);
            scoreDto.setPunctuality(Math.round(scoreDto.getPunctuality()/ feCnt*100)/100.0);
            scoreDto.setCommunication(Math.round(scoreDto.getCommunication()/ feCnt*100)/100.0);


            MyEvalListResponse response = MyEvalListResponse.builder()
                    .projectId(pm.getProject().getId())
                    .projectTitle(pm.getProject().getTitle())
                    .score(scoreDto)
                    .build();


//            List<BadgeDto> badgeList = midtermEvaluationRepository.findAllByMemberId(pm.getProject().getId(), memberId);
//            response.setBadgeDtos(badgeList);
            myEvalListResponseList.add(response);
        }



        return new CustomPageImpl<>(myEvalListResponseList, pageable, pmList.getTotalElements());
    }

    public MyEvalDetailResponse findMyEval(Long projectId, Long memberId) {
        List<BadgeDto> badgeList = midtermEvaluationRepository.findAllByMemberId(projectId, memberId);
        List<FinalTermEvaluation> myFinalEvalList = finalEvaluationRepository.findByProjectIdAndEvaluatedIdAndState(projectId, memberId, State.COMPLETE); // * 최종평가가 완료된 것만 조회, 임시저장 X
        List<FinalEvalDto> finalEvalDtoList = new ArrayList<>();
        ScoreDto scoreDto = new ScoreDto();
        for (FinalTermEvaluation fe : myFinalEvalList) {
            scoreDto.setCommunication(fe.getScore().getCommunication());
            scoreDto.setPunctuality(fe.getScore().getPunctuality());
            scoreDto.setSincerity(fe.getScore().getSincerity());
            scoreDto.setJobPerformance(fe.getScore().getJobPerformance());
            FinalEvalDto finalEvalDto = FinalEvalDto.builder()
                    .memberId(fe.getEvaluator().getId())
                    .memberName(fe.getEvaluator().getName())
                    .imageUrl(fe.getEvaluator().getImageUrl())
                    .content(fe.getContent())
                    .score(scoreDto)
                    .build();
            finalEvalDtoList.add(finalEvalDto);

        }



        return new MyEvalDetailResponse(badgeList, finalEvalDtoList);
    }

    public MyEvalChartResponse findMyEvalChart(Long memberId) {
        List<ScoreDao> myAvgScoreList = finalEvaluationRepository.findMyAvgScore(memberId);
         double sincerity =0;
         double jobPerformance=0;
         double punctuality=0;
         double communication=0;
        for (ScoreDao scoreDao : myAvgScoreList) {
            sincerity+=scoreDao.getSincerity();
            jobPerformance+=scoreDao.getJobPerformance();
            punctuality+=scoreDao.getPunctuality();
            communication+=scoreDao.getCommunication();
        }
        ScoreDto score = new ScoreDto();
        score.setSincerity(sincerity/myAvgScoreList.size());
        score.setJobPerformance(jobPerformance/myAvgScoreList.size());
        score.setPunctuality(punctuality/myAvgScoreList.size());
        score.setCommunication(communication/myAvgScoreList.size());

        // * 소수점 조정 임시 주석
//        score.setSincerity(Math.round((sincerity/myAvgScoreList.size())*100)/100.0);
//        score.setJobPerformance(Math.round(jobPerformance/myAvgScoreList.size()*100)/100.0);
//        score.setPunctuality(Math.round(punctuality/myAvgScoreList.size()*100)/100.0);
//        score.setCommunication(Math.round(communication/myAvgScoreList.size()*100)/100.0);
        return new MyEvalChartResponse(score);
    }


}
