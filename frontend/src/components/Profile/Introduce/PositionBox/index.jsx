/* eslint-disable react-hooks/exhaustive-deps */
import { useEffect, useState } from "react";
import Button from "../../../common/Button";
import SearchStack from "./SearchStack";
import { useProfile } from "../../../../context/ProfileContext";
import { toast } from "react-toastify";

import Select from "../../../../components/common/Select";
import { positionSelect } from "../../../../utils/optionData";
import StackItem from "../../../common/StackItem";
import { filterSelectStack } from "../../../../utils/searchStack/handleStackList";
import { useProfileClient } from "../../../../context/Client/ProfileClientContext";

const PositionBox = () => {
  const [isModify, setIsModify] = useState(false);
  const [positionAndStack, setPositionAndStack] = useState({
    position: "",
    stack: [],
  });
  const { isMyProfile, memberId } = useProfile();
  const { getPositionAPI, patchProfileAPI } = useProfileClient();

  useEffect(() => {
    const getPosition = async () => {
      const response = await getPositionAPI(memberId);
      if (response.data)
        setPositionAndStack((prev) => ({
          ...prev,
          position: response.data.recruitPosition,
          stack: response.data.techStack,
        }));
    };

    if (!isModify) getPosition();
  }, [memberId, isModify]);

  const submitModify = async () => {
    const submitStack = positionAndStack.stack.map((stack) => stack.name);

    const reqBody = {
      recruitPosition: positionAndStack.position,
      techStack: submitStack,
    };

    if (!reqBody.recruitPosition || !reqBody.techStack) {
      toast.error("개발 직무와 기술 스택을 반드시 선택해야합니다.");
    } else {
      await patchProfileAPI(reqBody);
      setIsModify(false);
      toast.success("수정되었습니다.");
    }
  };

  const changePosition = (event) => {
    setPositionAndStack((prev) => ({ ...prev, position: event.target.value }));
  };

  const changeStack = (stack) => {
    setPositionAndStack((prev) => ({
      ...prev,
      stack: [...prev.stack, stack],
    }));
  };

  const deleteStack = (stack) => {
    const result = filterSelectStack(positionAndStack.stack, stack);
    setPositionAndStack((prev) => ({
      ...prev,
      stack: [...result],
    }));
  };
  return (
    <div className="profile_body_introduce_Box">
      <div className="profile_body_introduce_Box_title">
        <h2>개발 직무</h2>
        <div className="profile_body_introduce_Box_title_btnBox">
          {isMyProfile ? (
            isModify ? (
              <>
                <Button
                  text="취소"
                  size="small"
                  onClick={() => setIsModify(false)}
                />
                <Button
                  type="submit"
                  text="완료"
                  size="small"
                  onClick={submitModify}
                />
              </>
            ) : (
              <Button
                text="수정"
                size="small"
                onClick={() => setIsModify(true)}
              />
            )
          ) : null}
        </div>
      </div>
      {isModify ? (
        <Select
          options={positionSelect}
          type="large"
          onChange={changePosition}
        />
      ) : (
        <div className="profile_body_introduce_positionBox_position">
          <span>{positionAndStack.position}</span>
        </div>
      )}

      <div className="profile_body_introduce_Box_title profile_mr-t">
        <h2>기술 스택</h2>
      </div>

      {isModify ? (
        <SearchStack
          stackList={positionAndStack.stack}
          changeStack={changeStack}
          memberId={memberId}
          deleteStack={deleteStack}
        />
      ) : positionAndStack.stack.length > 0 ? (
        <ul className="profile_body_introduce_positionBox_stack_bc_items_five">
          {positionAndStack.stack.map((stack, idx) => (
            <StackItem name={stack.name} imageUrl={stack.imageUrl} key={idx} />
          ))}
        </ul>
      ) : (
        <div className="no-stack-list">기술 스택을 작성해주세요.</div>
      )}
    </div>
  );
};
export default PositionBox;
