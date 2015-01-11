package impro.tc.flink;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by qmlmoon on 11/29/14.
 */
public class HttpConnection {

	public static void sendPost(News news, String category) throws Exception {
		String url = "http://192.168.2.106:8080/addnews";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("charset", "utf-8");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		String urlParameters = news.toString() + "&category=" + category;

		con.setDoOutput(true);
		con.setDoInput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		if (responseCode != 200) {
			throw new RuntimeException("message posting failed!");
		}
		con.disconnect();

	}
}
