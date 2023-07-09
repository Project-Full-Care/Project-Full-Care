import { useQuery } from "react-query";
import { useDispatch, useSelector } from "react-redux";
import { getMeetingRecord } from "../../lib/apis/meetingRecordManagementApi";
import useMeetingRecordManagementMutation from "../../hooks/useMeetingRecordManagementMutation";
import { meetingRecordManagementActions } from "../../redux/meetingRecordManagementSlice";
import { useLayoutEffect } from "react";
import Button from "../common/Button";

const MeetingRecord = ({ state }) => {
  const selectedMeetingRecordId = useSelector(
    (state) => state.meetingRecordManagement.selectedMeetingRecordId
  );

  const createdMeetingRecordId = useSelector(
    (state) => state.meetingRecordManagement.createdMeetingRecordId
  );

  const dispatch = useDispatch();

  const { deleteMutate, createBookMarkMutate } =
    useMeetingRecordManagementMutation();

  const fallback = {};
  const { data: createdData = fallback } = useQuery(
    ["managementCreatedMeetingRecordList", createdMeetingRecordId],
    () => getMeetingRecord(createdMeetingRecordId),
    {
      enabled: state === "createdMeetingRecord",
    }
  );

  const { data: selectedData = fallback } = useQuery(
    ["managementSelectedMeetingRecordList", selectedMeetingRecordId],
    () => getMeetingRecord(selectedMeetingRecordId),
    {
      enabled: state === "selectedMeetingRecord",
    }
  );

  const handleEditMeetingRecord = () => {
    dispatch(meetingRecordManagementActions.onChangeIsEditState(true));
    dispatch(
      meetingRecordManagementActions.onEditSelectedMeetingRecordState(false)
    );
    dispatch(
      meetingRecordManagementActions.onEditIsCreatedMeetingRecordVisibleState(false)
    );
  };

  const handleDeleteMeetingRecord = () => {
    state === "selectedMeetingRecord"
      ? deleteMutate(selectedData.memoId)
      : deleteMutate(createdData.memoId);
    dispatch(meetingRecordManagementActions.onEditInitialState(true));
  };

  const handleBookMarkMeetingRecord = () => {
    state === "selectedMeetingRecord"
      ? createBookMarkMutate(selectedData.memoId)
      : createBookMarkMutate(createdData.memoId);
    dispatch(meetingRecordManagementActions.onEditInitialState(true));
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
            <Button
              size={"small"}
              type={"underlined"}
              text={"삭제하기"}
              onClick={handleDeleteMeetingRecord}
            />
            <Button
              size={"small"}
              type={"underlined"}
              text={"수정하기"}
              onClick={handleEditMeetingRecord}
            />
            {selectedData.bookmarked ? (
              <Button
                size={"small"}
                type={"underlined"}
                text={"북마크 취소하기"}
                onClick={handleBookMarkMeetingRecord}
              />
            ) : (
              <Button
                size={"small"}
                type={"underlined"}
                text={"북마크하기"}
                onClick={handleBookMarkMeetingRecord}
              />
            )}
          </div>
        </div>
      </div>
      <div
        className="meeting-record-content"
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
              <Button
                size={"small"}
                type={"underlined"}
                text={"삭제하기"}
                onClick={handleDeleteMeetingRecord}
              />
              <Button
                size={"small"}
                type={"underlined"}
                text={"수정하기"}
                onClick={handleEditMeetingRecord}
              />
              {createdData.bookmarked ? (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"북마크 취소하기"}
                  onClick={handleBookMarkMeetingRecord}
                />
              ) : (
                <Button
                  size={"small"}
                  type={"underlined"}
                  text={"북마크하기"}
                  onClick={handleBookMarkMeetingRecord}
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