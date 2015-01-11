package impro.tc.flink;

import categorizer.Categorization;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import my.JSONReader;
import my.Pipeline;
import org.apache.commons.lang.SerializationUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.function.sink.SinkFunction;
import org.apache.flink.streaming.connectors.json.JSONParseFlatMap;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.util.Collector;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.CasCreationUtils;

import java.io.PrintWriter;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * Text categorization on top of apache flink streaming. The streaming data is retrieved from RMQ server.
 * When certain constraint is achieved, e.g. the news from certain country, it will call the visualization
 * function to display it in some way.
 * Currently it prints the message to stdout.
 */
public class TextCategorisation {

	public static final class HttpSink implements SinkFunction<Tuple2<News, String>> {

		@Override
		public void invoke(Tuple2<News, String> news) {
			System.out.println("starting news with tille [" + news.f0.getTitle() + "] to server...");
			try {
				HttpConnection.sendPost(news.f0, news.f1);
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

	public static final class GetCategory extends RichMapFunction<News, Tuple2<News, String>> {
		public CollectionReader reader = null;

		public CAS cas = null;

		public AnalysisEngine aae = null;

		@Override
		public void open(Configuration parameters) throws Exception {
			Runtime runtime = Runtime.getRuntime();
			System.out.println("free memory: " + runtime.freeMemory() / 1024 / 1024);
			System.out.println("total memory:" + runtime.totalMemory() / 1024 / 1024);

			CollectionReaderDescription readerDesc = createReaderDescription(JSONReader.class,        //1)
				TextReader.PARAM_SOURCE_LOCATION,
				System.getProperty("java.io.tmpdir") + "/tmp-news.txt",
				TextReader.PARAM_LANGUAGE, "de");

			AnalysisEngineDescription[] descs = new AnalysisEngineDescription[]{createEngineDescription(OpenNlpSegmenter.class),    //2)
				createEngineDescription(OpenNlpPosTagger.class),    //3)
				createEngineDescription(LanguageToolLemmatizer.class),    //4)
				createEngineDescription(StanfordNamedEntityRecognizer.class),    //5)
				createEngineDescription(MaltParser.class),        //6
				createEngineDescription(XmiWriter.class,        //7)
					XmiWriter.PARAM_TARGET_LOCATION, System.getProperty("java.io.tmpdir") + "/tmp-annotated.xmi")
			};

			ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();

			// Create the components
			reader = UIMAFramework.produceCollectionReader(readerDesc, resMgr, null);

			// Create AAE
			final AnalysisEngineDescription aaeDesc = createEngineDescription(descs);

			// Instantiate AAE
			aae = UIMAFramework.produceAnalysisEngine(aaeDesc, resMgr, null);

			// Create CAS from merged metadata

			cas = CasCreationUtils.createCas(asList(reader.getMetaData(), aae.getMetaData()));
			reader.typeSystemInit(cas.getTypeSystem());

		}

		@Override
		public Tuple2<News, String> map(News n) throws Exception {

			reader.getNext(cas);
			cas.setDocumentText(n.getNews());
			aae.process(cas);
			cas.reset();
			String ca = Categorization.run();
			System.out.println(ca);
			return new Tuple2<News, String>(n, ca);
		}
	}

	public static class FilterCategory implements FilterFunction<Tuple2<News, String>> {

		@Override
		public boolean filter(Tuple2<News, String> t) {
			if (t.f1 != null) {
				return true;
			} else {
				return false;
			}
		}
	}

	//---------------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setBufferTimeout(2000);
		@SuppressWarnings("unused")
		DataStream<Tuple2<News, String>> dataStream1 = env
			.addSource(new MyRMQSource("localhost", "hello"), Integer.parseInt(args[0]))
			.flatMap(new MyJSONParserFlatMap())
			.map(new GetCategory())
			.filter(new FilterCategory())
			.addSink(new HttpSink());

		env.execute();
	}



}
