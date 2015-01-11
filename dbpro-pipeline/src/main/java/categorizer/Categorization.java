package categorizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.ruta.engine.Ruta;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.ProcessTrace;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

public class Categorization {

	public static String[] cList = {"New.Naturkatastrophe", "New.Krankheit", "New.Krise"};
	/**
	 * Prints all Annotations of a specified Type to a PrintStream.
	 *
	 * @param aCAS
	 *            the CAS containing the FeatureStructures to print
	 * @param aAnnotType
	 *            the Type of Annotation to be printed
	 * @param aOut
	 *            the PrintStream to which output will be written
	 */
	public static String getCategory(CAS aCAS, Type aAnnotType,
										PrintStream aOut) {
		// get iterator over annotations
		FSIterator iter = aCAS.getAnnotationIndex(aAnnotType).iterator();
		// iterate
		Set<String> as = new HashSet<String>();
		while (iter.isValid()) {
			FeatureStructure fs = iter.get();
			as.add(fs.getType().getName());
			iter.moveToNext();
		}
		for (String s: cList) {
			if (as.contains(s)){
				return s.substring(4, s.length());
			}
		}
		return null;
	}


	public String run() throws IOException,
		InvalidXMLException, ResourceInitializationException,
		AnalysisEngineProcessException, ResourceConfigurationException,
		URISyntaxException, CASException, SAXException {
		File specFile = new File("/home/qmlmoon/Documents/text-categorisation/dbpro-pipeline/src/main/resources/descriptor/NewEngine.xml");
		XMLInputSource in = new XMLInputSource(specFile);
		ResourceSpecifier specifier = UIMAFramework.getXMLParser()
			.parseResourceSpecifier(in);
		// for import by name... set the datapath in the ResourceManager
		AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);
		CAS cas = ae.newCAS();
		File documentTextFile = new File(System.getProperty("java.io.tmpdir") + "/tmp-annotated.xmi/tmp-news.txt.xmi");
		InputStream xmiFile = new FileInputStream(documentTextFile);

		XmiCasDeserializer.deserialize(xmiFile, cas, true);
//		cas.setDocumentText(s);
		File scriptFile = new File("/home/qmlmoon/Documents/text-categorisation/dbpro-pipeline/src/main/resources/script/New.ruta");
		String scriptContent = (String) org.apache.commons.io.FileUtils
			.readFileToString(scriptFile, "UTF-8");
		Ruta.apply(cas, scriptContent);
		ProcessTrace pt = ae.process(cas);

		Type annotationType = cas.getTypeSystem().getType(
			CAS.TYPE_NAME_ANNOTATION);
		return getCategory(cas, annotationType, System.out);

	}

	public static void main(String[] args) throws Exception{
		System.out.println((new Categorization()).run());

	}

}