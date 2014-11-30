package impro.tc.webserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by qmlmoon on 11/30/14.
 */
@ComponentScan
@EnableAutoConfiguration
public class WebServer {
	public static void main(String[] args) {
		SpringApplication.run(WebServer.class, args);
	}
}
