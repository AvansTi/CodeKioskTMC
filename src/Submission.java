import javax.json.JsonObject;
import java.time.LocalDateTime;

public class Submission {
	int id;
	LocalDateTime creationTime;

	public Submission(JsonObject submission)
	{
		id = submission.getInt("id");
		String d = submission.getString("created_at");
		d = d.substring(0, d.indexOf("+"));
		creationTime = LocalDateTime.parse(d);
	}

	@Override
	public String toString() {
		return "Submission{" +
				"id=" + id +
				", creationTime=" + creationTime +
				'}';
	}
}
