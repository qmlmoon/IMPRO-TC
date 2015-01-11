package my;

import java.io.*;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import org.json.JSONObject;

/**
* UIMA collection reader for json files.
* Structure and some code copied from Textreader by Richard Eckart de Castilho.
* @author David Skowronek
*/
@TypeCapability(
       outputs={
               "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class JSONReader
	extends ResourceCollectionReaderBase
{
	/**
	 * Automatically detect encoding.
	 *
	 * @see CharsetDetector
	 */
	public static final String ENCODING_AUTO = "UTF-8";

	/**
	 * Name of configuration parameter that contains the character encoding used by the input files.
	 */
	public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;

	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	public Resource res = null;
	public BufferedReader br = null;
	public String line;
	public int c = 0;

	private String getNews(String s) {
		JSONObject js = new JSONObject(s);
		return js.getString("news");
	}


	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException{
//		c++;
//		initCas(aCAS, res, String.valueOf(c));
//		System.out.println(c);
//		aCAS.setDocumentText(getNews(line));
		if (res == null) {
			res = getResourceIterator().next();
		}
		initCas(aCAS, res);

	}

	public void getNext(CAS aCAS, String s)
		throws IOException, CollectionException {
		if (res == null) {
			res = getResourceIterator().next();
		}
		initCas(aCAS, res);
	}

	@Override
	public boolean hasNext() throws IOException {
		if (res == null) {
			res = getResourceIterator().next();
			br = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
		}
		if ((line = br.readLine()) != null) {
			return true;
		} else {
			return false;
		}
	}
}
