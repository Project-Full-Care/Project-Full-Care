import { useState } from "react";
import { useLocation } from "react-router-dom";

import { toast } from "react-toastify";
import { getProjectId } from "../../utils/getProjectId";

import Button from "../common/Button";
import ControlMenu from "../common/ControlMenu";
import ModalContainer from "../common/Modal/ModalContainer";
import AlertCheckModal from "../common/Modal/AlertCheckModal";

import enthusiasticParticipantBadgeImgUrl from "../../assets/enthusiastic-participant-badge-img.svg";
import goodLeaderBadgeImgUrl from "../../assets/good-leader-badge-img.svg";
import ideaBankBadgeImgUrl from "../../assets/idea-bank-badge-img.svg";
import bestSupporterBadgeImgUrl from "../../assets/best-supporter-badge-img.svg";
import { useMediaQuery } from "@mui/material";
import useEvaluationManagementMutation from "../../hooks/Mutations/useEvaluationManagementMutation";

const evaluationCriterion = [
  {
    name: "성실도",
    value: "sincerity",
  },
  {
    name: "시간 엄수",
    value: "punctuality",
  },
  {
    name: "업무 수행 능력",
    value: "jobPerformance",
  },
  {
    name: "의사 소통",
    value: "communication",
  },
];

const evaluationOptionList = [
  {
    id: 0,
    value: 0,
    name: "0",
  },
  {
    id: 1,
    value: 1,
    name: "1",
  },
  {
    id: 2,
    value: 2,
    name: "2",
  },
  {
    id: 3,
    value: 3,
    name: "3",
  },
  {
    id: 4,
    value: 4,
    name: "4",
  },
  {
    id: 5,
    value: 5,
    name: "5",
  },
];

const FinalEvaluation = ({
  type,
  participantId,
  member,
  setIsFinalEvaluationVisible,
  isFinalEvaluationVisible,
  badgeQuantity,
  data,
}) => {
  const [sincerityScore, setSincerityScore] = useState(0);
  const [punctualityScore, setPunctualityScore] = useState(0);
  const [jobPerformanceScore, setJobPerformanceScore] = useState(0);
  const [communicationScore, setCommunicationScore] = useState(0);

  const [isAlertModalVisible, setIsAlertModalVisible] = useState(false);

  const projectId = getProjectId(useLocation());

  const [content, setContent] = useState("");

  const handleModalClose = () => setIsFinalEvaluationVisible(false);

  const { finalEvaluationMutate } = useEvaluationManagementMutation();

  const handleChangeContent = (e) => {
    setContent(e.target.value);
  };

  const handleSubmitFinalEvaluation = () => {
    if (content.length < 1) {
      toast.error("최종 의견을 작성해주세요.");
      return;
    }

    setIsAlertModalVisible(true);
  };

  const getValue = (idx) => {
    switch (idx) {
      case 0:
        return sincerityScore;
      case 1:
        return punctualityScore;
      case 2:
        return jobPerformanceScore;
      case 3:
        return communicationScore;
      default:
        return 0;
    }
  };

  const getOnChange = (idx) => {
    switch (idx) {
      case 0:
        return setSincerityScore;
      case 1:
        return setPunctualityScore;
      case 2:
        return setJobPerformanceScore;
      case 3:
        return setCommunicationScore;
      default:
        return 0;
    }
  };

  const getBadgeImgUrl = (idx) => {
    switch (idx) {
      case 0:
        return goodLeaderBadgeImgUrl;
      case 1:
        return enthusiasticParticipantBadgeImgUrl;
      case 2:
        return bestSupporterBadgeImgUrl;
      case 3:
        return ideaBankBadgeImgUrl;
      default:
        return "";
    }
  };

  const isMobile = useMediaQuery("(max-width: 767px)");
  const isTablet = useMediaQuery("(min-width: 767px) and (max-width: 1024px)");

  return (
    <ModalContainer
      open={isFinalEvaluationVisible}
      onClose={handleModalClose}
      type={"dark"}
      width={isMobile ? "95%" : isTablet ? 700 : 800}
      height={isMobile ? "70%" : "90%"}
    >
      {type === "evaluation" ? (
        <div className="evaluation-management-final-evaluation">
          <div className="final-evaluation-heading">
            '{member}' 최종 평가하기
          </div>
          <div className="final-evaluation-body">
            <div className="evaluation">
              <h1>평가하기</h1>
              <div className="evaluation-criterion">
                {evaluationCriterion.map((criterion, idx) => (
                  <div key={idx}>
                    {criterion.name}
                    <ControlMenu
                      size="small"
                      optionList={evaluationOptionList}
                      value={getValue(idx)}
                      onChange={getOnChange(idx)}
                    />
                  </div>
                ))}
              </div>
            </div>
            <div className="evaluation-badges">
              <h1>누적 배지</h1>
              <div className="evaluation-badges-body">
                {badgeQuantity?.map((badge, idx) => (
                  <div className="badge" key={idx}>
                    <figure
                      style={{
                        backgroundImage: `url(${getBadgeImgUrl(idx)})`,
                      }}
                    />
                    <div>{badge.evaluationBadge}</div>
                    <div className="badge-quantity">{badge.quantity} 개</div>
                  </div>
                ))}
              </div>
            </div>
            <div className="content">
              <h1>최종 의견 작성하기</h1>
              <div>
                <div>'{member}'에 대한 최종 의견입니다.</div>
                <textarea
                  value={content}
                  onChange={handleChangeContent}
                  placeholder={`${member}에 대한 최종 평가를 글로 써보세요!`}
                />
              </div>
            </div>
          </div>
          <div className="final-evaluation-button-wrapper">
            <Button
              text={"평가 완료하기"}
              size={"small"}
              onClick={handleSubmitFinalEvaluation}
            />
          </div>
        </div>
      ) : (
        <div className="evaluation-management-final-evaluation">
          <div className="final-evaluation-heading">
            '{member}' 최종 평가 보기
          </div>
          <div className="final-evaluation-body">
            <div className="evaluation">
              <h1>평가</h1>
              <div className="evaluation-criterion">
                {evaluationCriterion.map((criterion, idx) => (
                  <div key={idx}>
                    {criterion.name}
                    <div>{data.score[criterion.value]} 점</div>
                  </div>
                ))}
              </div>
            </div>
            <div className="evaluation-badges">
              <h1>누적 배지</h1>
              <div className="evaluation-badges-body">
                {badgeQuantity?.map((badge, idx) => (
                  <div className="badge" key={idx}>
                    <figure
                      style={{
                        backgroundImage: `url(${getBadgeImgUrl(idx)})`,
                      }}
                    />
                    <div>{badge.evaluationBadge}</div>
                    <div className="badge-quantity">{badge.quantity} 개</div>
                  </div>
                ))}
              </div>
            </div>
            <div className="content">
              <h1>최종 의견</h1>
              <div>
                <div>'{member}'에 대한 최종 의견입니다.</div>
                <textarea value={data.content} readOnly />
              </div>
            </div>
          </div>
        </div>
      )}
      {isAlertModalVisible && (
        <AlertCheckModal
          onClose={() => setIsAlertModalVisible(false)}
          open={isAlertModalVisible}
          text={
            "최종 평가는 작성 후 수정 / 삭제가 불가합니다. 작성 완료하시겠습니까?"
          }
          width={380}
          height={210}
          clickHandler={() => {
            finalEvaluationMutate({
              projectId: projectId,
              evaluatedId: participantId,
              score: {
                sincerity: sincerityScore,
                punctuality: punctualityScore,
                jobPerformance: jobPerformanceScore,
                communication: communicationScore,
              },
              content: content,
            });

            setIsFinalEvaluationVisible(false);
          }}
        />
      )}
    </ModalContainer>
  );
};

export default FinalEvaluation;
