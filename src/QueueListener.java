import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QueueListener {
	LocalDateTime lastSubmission = null;
	List<Submission> currentSubmissions = new ArrayList<>();
	List<Integer> playedSubmissions = new ArrayList<>();



	public QueueListener()
	{

	}


	public int nextSubmission() throws IOException {
		updateSubmissions();

		Submission toPlay = null;
		for(Submission submission : currentSubmissions)
			if(submission.creationTime.isAfter(lastSubmission))
				toPlay = submission;
		if(toPlay != null) // if there's a newer submission
		{
			System.out.println("Found a newer submission submitted =)");
			lastSubmission = toPlay.creationTime;
			return toPlay.id;
		}

		List<Submission> unplayedSubmissions = currentSubmissions.stream().filter(s -> playedSubmissions.indexOf(s.id) == -1).collect(Collectors.toList());
		if(unplayedSubmissions.isEmpty())
		{
			playedSubmissions.clear();
			unplayedSubmissions = currentSubmissions.stream().filter(s -> playedSubmissions.indexOf(s.id) == -1).collect(Collectors.toList());
		}
		int index = 0;
		playedSubmissions.add(unplayedSubmissions.get(index).id);
		return unplayedSubmissions.get(index).id;
		//return unplayedSubmissions.get((int)(Math.random() * unplayedSubmissions.size())).id;
	}

	public void getSubmissionZip(int id) throws IOException {
		String fileName = "submissions/" + id + ".zip";
		if(!Files.exists(Paths.get(fileName))) {
			String data = Util.get("https://tmc.mooc.fi/api/v8/core/submissions/" + id + "/download");
			Files.write(Paths.get(fileName), data.getBytes(StandardCharsets.ISO_8859_1));
		}

	}

	private void updateSubmissions() throws IOException {
		JsonArray submissions = Json.createReader(new ByteArrayInputStream(Util.get("https://tmc.mooc.fi/api/v8/courses/376/submissions?1").getBytes())).readArray();
		currentSubmissions.clear();
		for(JsonValue s : submissions)
		{
			JsonObject submission = (JsonObject)s;
			currentSubmissions.add(new Submission(submission));
		}

		Collections.sort(currentSubmissions, Comparator.comparing(o -> o.creationTime));

		if(lastSubmission == null)
			lastSubmission = currentSubmissions.get(0).creationTime;
		System.out.println("Submissions: " + currentSubmissions.size());
	}


}
