import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Util {


	public static String get(String url) throws IOException {
		URL reqUrl = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) reqUrl.openConnection();

		con.setRequestProperty("Cookie", Files.readAllLines(Paths.get("cookie.txt")).get(0));

		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.setDoInput(true);

		StringBuilder result = new StringBuilder();
		InputStream in = con.getInputStream();

		while(true) {
			int b = in.read();
			if(b < 0)
				break;
			result.append((char)b);
		}
		return result.toString();
	}


	public static String post(String url, String payload) throws IOException {
		URL reqUrl = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) reqUrl.openConnection();

		con.setRequestProperty("Cookie", Files.readAllLines(Paths.get("cookie.txt")).get(0));


		con.setRequestProperty("Content-Type","application/json");
		con.setRequestProperty("Content-length", payload.length() + "");
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);

		OutputStreamWriter output = new OutputStreamWriter(con.getOutputStream());
		output.write(payload);
		output.close();

		StringBuilder result = new StringBuilder();
		InputStream in = con.getInputStream();

		while(true) {
			int b = in.read();
			if(b < 0)
				break;
			result.append((char)b);
		}
		return result.toString();
	}
}
