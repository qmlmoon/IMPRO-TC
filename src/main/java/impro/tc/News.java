package impro.tc;

import org.apache.flink.api.java.tuple.Tuple6;

/**
 * Data type of the given news.
 */
public class News extends Tuple6<String, String, String, String, String, String> {

	public News() {}

	public void setNews(String s) { this.setField(s, 0);}
	public void setDataString(String s) { this.setField(s, 1);}
	public void setTitle(String s) { this.setField(s, 2);}
	public void setSubTitle(String s) { this.setField(s, 3);}
	public void setSrcUrl(String s) { this.setField(s, 4);}
	public void setSrcName(String s) { this.setField(s, 5);}

	public String getNews() { return this.getField(0); }
	public String getDataString() { return this.getField(1);}
	public String getTitle() { return this.getField(2);}
	public String getSubTitle() { return this.getField(3);}
	public String getSrcUrl() { return this.getField(4);}
	public String getSrcName() { return this.getField(5);}
}