package impro.tc;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.commons.lang.SerializationUtils;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.function.sink.SinkFunction;
import org.apache.flink.streaming.connectors.json.JSONParseFlatMap;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.util.Collector;

import java.io.File;
import java.util.List;

/**
 * Text categorization on top of apache flink streaming. The streaming data is retrieved from RMQ server.
 * When certain constraint is achieved, e.g. the news from certain country, it will call the visualization
 * function to display it in some way.
 * Currently it prints the message to stdout.
 */
public class TextCategorisation {

	public static final class MyRMQPrintSink implements SinkFunction<News> {

		@Override
		public void invoke(News news) {
			System.out.println("starting sending news to server...");
			try {
				HttpConnection.sendPost(news);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static final class MyRMQSource extends RMQSource<String> {

		public MyRMQSource(String HOST_NAME, String QUEUE_NAME) {
			super(HOST_NAME, QUEUE_NAME);
		}

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
			cNews.setDateString(date);
			cNews.setTitle(title);
			cNews.setSubTitle(subTitle);
			cNews.setSrcUrl(srcUrl);
			cNews.setSrcName(srcName);

			out.collect(cNews);
		}
	}

	public static final class FilterCountry extends RichFilterFunction<News> {

		private String country = null;
		private transient CRFClassifier<CoreLabel> crfClassifier;

		public FilterCountry(String country) {
			this.country = country;
		}

		@Override
		public void open(Configuration parameters) throws Exception {
//			System.out.println("starting loading classifier...");
//			Runtime runtime = Runtime.getRuntime();
//			System.out.println("free memory: " + runtime.freeMemory() / 1024 / 1024);
//			System.out.println("total memory:" + runtime.totalMemory() / 1024 / 1024);
//			crfClassifier = CRFClassifier.getClassifier(new File("/home/qmlmoon/Documents/text-categorisation/resource/hgc_175m_600.crf.ser.gz"));
//			System.out.println("finishing loading classifier...");
		}
		@Override
		public boolean filter(News n) throws Exception {
//			List<List<CoreLabel>> NerOfNews = crfClassifier.classify(n.getNews());
//
//			for (List<CoreLabel> sentence : NerOfNews)
//				for (CoreLabel word : sentence) {
//					if (word.get(CoreAnnotations.AnswerAnnotation.class).toString().equals("I-LOC")) {
//						if (word.word().toUpperCase().equals(this.country)) {
//							return true;
//						}
//					}
//				}


//			if (this.country != null && n.getNews().toUpperCase().contains(this.country) != true) {
//				return false;
//			} else {
//				return true;
//			}
			return true;
		}
	}

	//---------------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setBufferTimeout(2000);
		@SuppressWarnings("unused")
		DataStream<News> dataStream1 = env
			.addSource(new MyRMQSource("localhost", "hello"), Integer.parseInt(args[0]))
			.flatMap(new MyJSONParserFlatMap())
			.filter(new FilterCountry("FRANKFURT"))
			.addSink(new MyRMQPrintSink());


//		dataStream1.count()
//			.print();

		env.execute();
	}

}
