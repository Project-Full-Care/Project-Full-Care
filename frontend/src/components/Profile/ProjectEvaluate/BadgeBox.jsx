// badgeDtos : {evaluationBadge: string, quantity: number}[]

const BadgeBox = ({ badgeDtos }) => {
  return (
    <section className="badgeBox">
      <div>
        <h2 className="projectEvaluate_title">중간평가 뱃지</h2>
      </div>
      <div className="badges">
        {badgeDtos.map((badge, idx) => (
          <div key={idx} className="badge">
            <div>
              <div className="badgeImage"></div>
            </div>
            <div>
              <div className="badge_divide"></div>
            </div>
            <div className="badge_quantity">
              <span className="badge_quantity_score">{badge.quantity}</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

export default BadgeBox;

/*
{
  "badgeDtos": [
    {
      "evaluationBadge": "열정적인_참여자",
      "quantity": 0
    }
  ],
  "finalEvals": [
    {
      "memberId": 0,
      "memberName": "string",
      "imageUrl": "string",
      "content": "string",
      "score": {
        "sincerity": 0,
        "jobPerformance": 0,
        "punctuality": 0,
        "communication": 0
      }
    }
  ]
}
*/