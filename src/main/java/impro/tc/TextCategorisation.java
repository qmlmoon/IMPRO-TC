package impro.tc;

import org.apache.commons.lang.SerializationUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.function.sink.SinkFunction;
import org.apache.flink.streaming.connectors.json.JSONParseFlatMap;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.util.Collector;

/**
 * Text categorization on top of apache flink streaming. The streaming data is retrieved from RMQ server.
 * When certain constraint is achieved, e.g. the news from certain country, it will call the visualization
 * function to display it in some way.
 * Currently it prints the message to stdout.
 */
public class TextCategorisation {

	public static final class MyRMQPrintSink implements SinkFunction<News> {
		private static final long serialVersionUID = 1L;

		@Override
		public void invoke(News news) {
			System.out.println(news.getNews());
		}

	}

	public static final class MyRMQSource extends RMQSource<String> {

		public MyRMQSource(String HOST_NAME, String QUEUE_NAME) {
			super(HOST_NAME, QUEUE_NAME);
		}

		private static final long serialVersionUID = 1L;

		@Override
		public String deserialize(byte[] t) {
			String s = (String) SerializationUtils.deserialize(t);
			if (s.equals("q")) {
				closeWithoutSend();
			}
			return s;
		}

	}

	public static final class MyJSONParserFlatMap extends
		JSONParseFlatMap<String,News> {
		public final News cNews = new News();

		@Override
		public void flatMap(String value, Collector<News> out) throws Exception {
			String news = this.getString(value, "news");
			String date = this.getString(value, "dateString");
			String title = this.getString(value, "title");
			String subTitle = this.getString(value, "subtitle");
			String srcUrl = this.getString(value, "srcUrl");
			String srcName = this.getString(value, "srcName");

			cNews.setNews(news);
			cNews.setDataString(date);
			cNews.setTitle(title);
			cNews.setSubTitle(subTitle);
			cNews.setSrcUrl(srcUrl);
			cNews.setSrcName(srcName);

			out.collect(cNews);
		}
	}

	public static final class FilterCountry implements FilterFunction<News> {

		private String country = null;

		public FilterCountry(String country) {
			this.country = country;
		}

		public FilterCountry() {}

		@Override
		public boolean filter(News n) throws Exception {
			if (this.country != null && n.getNews().toUpperCase().contains(this.country) != true) {
				return false;
			} else {
				return true;
			}
		}
	}

	//---------------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(4);
		env.setBufferTimeout(2000);
		@SuppressWarnings("unused")
		DataStream<News> dataStream1 = env
			.addSource(new MyRMQSource("localhost", "hello"))
			.flatMap(new MyJSONParserFlatMap())
			.filter(new FilterCountry("SCHWEIZ"))
			.addSink(new MyRMQPrintSink());


//		dataStream1.count()
//			.print();

		env.execute();
	}

}
