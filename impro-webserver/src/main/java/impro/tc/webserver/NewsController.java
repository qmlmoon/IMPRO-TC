package impro.tc.webserver;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by qmlmoon on 11/30/14.
 */
@RestController
public class NewsController {

	private static List<News> newsQueue = new ArrayList<News>();

	@RequestMapping(method = RequestMethod.POST, value="/addnews")
	public void addNews(@RequestParam(value="news") String news,
						@RequestParam(value="date") String date,
						@RequestParam(value="title") String title,
						@RequestParam(value="subtitle") String subtitle,
						@RequestParam(value="url") String url,
						@RequestParam(value="src") String src) {
		newsQueue.add(new News(news, date, title, subtitle, url, src));
	}

	@RequestMapping(method = RequestMethod.GET, value="/getnews")
	public List<News> getNews() {
		return reverseReturn(newsQueue);
	}


	public static List<News> reverseReturn(List<News> alist) {
		if ( alist == null || alist.isEmpty()) {
			return null;
		}

		List<News> rlist = new ArrayList<News>(alist);

		Collections.reverse(rlist);
		return rlist;
	}
}
