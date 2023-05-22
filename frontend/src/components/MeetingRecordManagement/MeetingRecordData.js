import { useDispatch } from "react-redux";
import { meetingRecordManagementActions } from "../../redux/meetingRecordManagementSlice";

const MeetingRecordData = ({ sortedMeetingRecordList, isLoading }) => {
    const dispatch = useDispatch();

    const handleClickMeetingRecord = (e) => {
        dispatch(meetingRecordManagementActions.onEditSelectedMeetingRecord({
            id: parseInt(e.currentTarget.id),
            writer: e.currentTarget.getAttribute('writer'),
            date: new Date(parseInt(e.currentTarget.getAttribute('date'))).getTime(),
            title: e.currentTarget.getAttribute('title'),
            content: e.currentTarget.getAttribute('content'),
            bookMarked: false,
        }));
    }

    return (
        <div className='meeting-record-all-meeting-record-list-data'>
            {sortedMeetingRecordList().map((record) => (
                <div
                    className='meeting-record-item'
                    key={record.id}
                    onClick={handleClickMeetingRecord}
                    id={record.id}
                    writer={record.writer}
                    date={record.date}
                    title={record.title}
                    content={record.content}
                >
                    <div className='meeting-record-item-date'>
                        {new Date(record.date).toLocaleDateString()}
                    </div>
                    <div className='meeting-record-item-title'>
                        {record.title}
                    </div>
                    <div className='meeting-record-item-writer'>
                        {record.writer}
                    </div>
                </div>
            ))}
        </div>
    )
}

export default MeetingRecordData;