package impro.tc.flink;

import org.apache.commons.lang.SerializationUtils;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Send the Local batch news data to RMQ server to simulate the stream.
 */
public class SimulateStreamNews {

	public static final class MyRMQSink extends RMQSink<String> {
		public MyRMQSink(String HOST_NAME, String QUEUE_NAME) {
			super(HOST_NAME, QUEUE_NAME);
		}

		private static final long serialVersionUID = 1L;

		@Override
		public byte[] serialize(String t) {
			if (t.equals("q")) {
				sendAndClose();
			}
			return SerializationUtils.serialize((String) t);
		}

	}

	public static void main(String[] args) throws IOException {
		RMQSink<String> mySink = new MyRMQSink("localhost", "hello");
		mySink.initializeConnection();

		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String line;
		while ((line = br.readLine()) != null) {
			mySink.invoke(line);
		}
		br.close();
		mySink.closeWithoutSend();
	}
}
