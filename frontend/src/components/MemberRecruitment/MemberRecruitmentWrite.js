import { Link } from "react-router-dom";
import { useState } from "react";

import ArrowBackIosNewIcon from "@mui/icons-material/ArrowBackIosNew";
import AddCircleIcon from "@mui/icons-material/AddCircle";

import Button from "../common/Button";
import Card from "../common/Card";
import { backendStacks, designStacks, frontendStacks } from "./data/stack";

const MemberRecruitmentWrite = () => {
  // 기술 스택들 태그들
  const [stacks, setStacks] = useState([]);

  // 입력받은 스택
  const [stackInput, setStackInput] = useState("");

  // 입력한 값이 포함한 stack 보여주기
  const filteredStacks = backendStacks.filter((stack) =>
    stack.includes(stackInput.toLowerCase())
  );

  const handleStackInputChange = (e) => {
    setStackInput(e.target.value);
  };
  const stackPlusClickHandler = () => {
    if (stacks.includes(stackInput)) {
      return;
    }
    setStacks((prevState) => [...prevState, stackInput]);
    setStackInput("");
  };
  const stackMinusClickHandler = (project) => {
    setStacks((prevState) => prevState.filter((stack) => stack !== project));
  };

  return (
    <div className="member-write">
      <div className="member-title">
        <Link to="/recruitment">
          <ArrowBackIosNewIcon className="recruitment-direction" />
        </Link>
        <img
          src="https://cdn.pixabay.com/photo/2015/06/24/15/45/hands-820272_640.jpg"
          alt=""
        />
        <h2>New Project</h2>
      </div>

      <Card className="member-write-card">
        <div className="member-content">
          <div className="member-content-project">
            <h3>프로젝트 선택</h3>
            <select className="member-select1">
              <option disabled selected hidden>
                선택하기
              </option>
              <option>사이드 프로젝트1</option>
              <option>사이드 프로젝트2</option>
            </select>
          </div>

          <div className="member-content-project">
            <h3>주제/분야</h3>
            <select className="member-select2">
              <option>주제1</option>
              <option>주제2</option>
            </select>
          </div>

          <div className="member-content-project">
            <h3>모집 기간</h3>
            <input
              className="member-select3"
              type="date"
              required
              //value={startDate}
              //onChange={handleChangeStartDate}
              data-placeholder="시작 일자"
            />
            ~
            <input
              type="date"
              required
              //value={endDate}
              //onChange={handleChangeEndDate}
              data-placeholder="종료 일자"
            />
          </div>

          <div className="member-content-options">
            <h3>지역</h3>
            <select className="member-select4">
              <option>주제1</option>
              <option>주제2</option>
            </select>
          </div>

          <div className="member-content-position">
            <h3>포지션</h3>
            <div className="member-content-position-container">
              <div className="member-stack">
                <h5>백엔드</h5>
                <input
                  className="position-number"
                  type="number"
                  min="0"
                  placeholder="0"
                />
              </div>

              <div className="member-stack">
                <h5>프론트엔드</h5>
                <input
                  className="position-number"
                  type="number"
                  min="0"
                  placeholder="0"
                />
              </div>

              <div className="member-stack">
                <h5>디자인</h5>
                <input
                  className="position-number"
                  type="number"
                  min="0"
                  placeholder="0"
                />
              </div>

              <div className="member-stack">
                <h5>기획</h5>
                <input
                  className="position-number"
                  type="number"
                  min="0"
                  placeholder="0"
                />
              </div>
            </div>

            {/*스택 입력하는 칸*/}
            <div className="member-content-position-stack">
              <h5>기술 스택</h5>
              <div className="member-stack">
                <div className="member-stack-input">
                  <input
                    placeholder="기술 스택을 추가하세요"
                    type="text"
                    value={stackInput}
                    onChange={handleStackInputChange}
                  />
                  <AddCircleIcon
                    className="mui-icon"
                    onClick={stackPlusClickHandler}
                  />
                </div>
                {stackInput && (
                  <div className="member-inputcontainer-box">
                    {filteredStacks.map((stack, index) => (
                      <h5 key={index} onClick={() => setStackInput(stack)}>
                        {stack}
                      </h5>
                    ))}
                  </div>
                )}
              </div>
              <div className="member-stack-button">
                {stacks.length > 0 &&
                  stacks.map((stack) => (
                    <Button
                      text={stack}
                      size="small"
                      onClick={() => stackMinusClickHandler(stack)}
                    />
                  ))}
                {stacks.length === 0 && (
                  <h6>프로젝트에서 사용할 스택을 추가해보세요</h6>
                )}
              </div>
            </div>
          </div>

          <div className="member-content-description">
            <h3>설명</h3>
            <textarea />
          </div>

          <div className="member-content-description">
            <h3>레퍼런스</h3>
            <textarea />
            <input className="member-file" placeholder="파일을 첨부하세요" />
          </div>

          <div className="member-content-description">
            <h3>컨택</h3>
            <textarea />
          </div>
        </div>
      </Card>
      <Button text="수정 완료" />
    </div>
  );
};
export default MemberRecruitmentWrite;
