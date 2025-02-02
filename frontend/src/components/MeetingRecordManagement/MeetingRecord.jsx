import { useQuery } from "react-query";
import { useDispatch, useSelector } from "react-redux";
import useMeetingRecordManagementMutation from "../../hooks/Mutations/useMeetingRecordManagementMutation";
import { meetingRecordManagementActions } from "../../redux/meetingRecordManagementSlice";
import { useLayoutEffect } from "react";
import Button from "../common/Button";
import { getProjectId } from "../../utils/getProjectId";
import { useLocation } from "react-router-dom";
import { useManagementClient } from "../../context/Client/ManagementClientContext";

const MeetingRecord = ({ state }) => {
  const selectedMeetingRecordId = useSelector(
    (state) => state.meetingRecordManagement.selectedMeetingRecordId
  );

  const createdMeetingRecordId = useSelector(
    (state) => state.meetingRecordManagement.createdMeetingRecordId
  );

  const dispatch = useDispatch();

  const projectId = getProjectId(useLocation());

  const { getCompleteProjectData, getMeetingRecord } = useManagementClient();

  const { data: completeData } = useQuery(
    ["completeProjectData", projectId],
    () => getCompleteProjectData(projectId)
  );

  const { deleteMutate, createBookMarkMutate } =
    useMeetingRecordManagementMutation();

  const fallback = {};
  const { data: createdData = fallback } = useQuery(
    ["managementCreatedMeetingRecordList", createdMeetingRecordId],
    () => getMeetingRecord(createdMeetingRecordId, projectId),
    {
      enabled: state === "createdMeetingRecord",
    }
  );

  const { data: selectedData = fallback } = useQuery(
    ["managementSelectedMeetingRecordList", selectedMeetingRecordId],
    () => getMeetingRecord(selectedMeetingRecordId, projectId),
    {
      enabled: state === "selectedMeetingRecord",
    }
  );

  const handleEditMeetingRecord = () => {
    dispatch(meetingRecordManagementActions.setIsEditState(true));
    dispatch(
      meetingRecordManagementActions.setSelectedMeetingRecordState(false)
    );
    dispatch(
      meetingRecordManagementActions.setIsCreatedMeetingRecordVisibleState(
        false
      )
    );
  };

  const handleDeleteMeetingRecord = () => {
    state === "selectedMeetingRecord"
      ? deleteMutate({ meetingRecordId: selectedData.memoId, projectId })
      : deleteMutate({ meetingRecordId: createdData.memoId, projectId });
    dispatch(meetingRecordManagementActions.setInitialState(true));
  };

  const handleBookMarkMeetingRecord = (type) => {
    state === "selectedMeetingRecord"
      ? createBookMarkMutate({
          meetingRecordId: selectedData.memoId,
          projectId,
          type,
        })
      : createBookMarkMutate({
          meetingRecordId: createdData.memoId,
          projectId,
          type,
        });
    dispatch(meetingRecordManagementActions.setInitialState(true));
  };

  useLayoutEffect(() => {
    if (state === "selectedMeetingRecord") {
      dispatch(meetingRecordManagementActions.setTitle(selectedData.title));
      dispatch(meetingRecordManagementActions.setContent(selectedData.content));
    } else {
      dispatch(meetingRecordManagementActions.setTitle(createdData.title));
      dispatch(meetingRecordManagementActions.setContent(createdData.content));
    }
  }, [
    createdData.content,
    createdData.title,
    dispatch,
    selectedData.content,
    selectedData.title,
    state,
  ]);

  return state === "selectedMeetingRecord" && selectedData.createdDate ? (
    <div className="meeting-record">
      <div className="meeting-record-heading">
        <div className="meeting-record-date">
          {new Date(selectedData.createdDate).toLocaleString()}
        </div>
        <div className="meeting-record-title">{selectedData.title}</div>
        <div className="meeting-record-container">
          <div className="meeting-record-author">{selectedData.author}</div>
          <div className="meeting-record-button-wrapper">
            {!completeData && (
              <>
                {selectedData.deletable && (
                  <Button
                    size={"small"}
                    type={"underlined"}
                    text={"삭제"}
                    onClick={handleDeleteMeetingRecord}
                  />
                )}
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"수정"}
                  onClick={handleEditMeetingRecord}
                />
              </>
            )}
            {!completeData ? (
              selectedData.bookmarked ? (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"북마크 취소"}
                  onClick={() => handleBookMarkMeetingRecord("cancel")}
                />
              ) : (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"북마크"}
                  onClick={() => handleBookMarkMeetingRecord("bookmark")}
                />
              )
            ) : null}
          </div>
        </div>
      </div>
      <div
        className="created-meeting-record-content"
        dangerouslySetInnerHTML={{ __html: selectedData.content }}
      />
    </div>
  ) : (
    state === "createdMeetingRecord" && createdData.createdDate && (
      <div className="meeting-record">
        <div className="meeting-record-heading">
          <div className="meeting-record-date">
            {new Date(createdData.createdDate).toLocaleString()}
          </div>
          <div className="meeting-record-title">{createdData.title}</div>
          <div className="meeting-record-container">
            <div className="meeting-record-author">{createdData.author}</div>
            <div className="meeting-record-button-wrapper">
              {createdData.deletable && (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"삭제"}
                  onClick={handleDeleteMeetingRecord}
                />
              )}
              <Button
                size={"small"}
                type={"underlined"}
                text={"수정"}
                onClick={handleEditMeetingRecord}
              />
              {createdData.bookmarked ? (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"북마크 취소"}
                  onClick={() => handleBookMarkMeetingRecord("cancel")}
                />
              ) : (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"북마크"}
                  onClick={() => handleBookMarkMeetingRecord("bookmark")}
                />
              )}
            </div>
          </div>
        </div>
        <div
          className="selected-meeting-record-content"
          dangerouslySetInnerHTML={{ __html: createdData.content }}
        />
      </div>
    )
  );
};

export default MeetingRecord;
