package impro.tc.webserver;

/**
 * Created by qml_moon on 29/11/14.
 */
public class News {
	private String news;
	private String date;
	private String title;
	private String subtitle;
	private String url;
	private String src;
	private String category;

	public News(String news, String date, String title, String subtitle, String url, String src, String category) {
		this.news = news;
		this.date = date;
		this.title = title;
		this.subtitle = subtitle;
		this.url = url;
		this.src = src;
		this.category = category;
	}

	public String getNews() {
		return this.news;
	}

	public String getDate() {
		return this.date;
	}

	public String getTitle() {
		return this.title;
	}

	public String getSubtitle() {
		return this.subtitle;
	}

	public String getUrl() {
		return this.url;
	}

	public String getSrc() {
		return this.src;
	}

	public String getCategory() { return this.category; }
}