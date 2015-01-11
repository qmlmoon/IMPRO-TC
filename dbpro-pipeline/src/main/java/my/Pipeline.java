package my;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.*;
import org.apache.uima.ruta.engine.RutaEngine;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLInputSource;
import org.languagetool.Language;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Pipeline-Aufbau:
/*
1) Reader
2) Segmenter						Erstellung von Token (Wörter, Satzzeichen, ...)
3) POS-Tagger (PartOfSpeech-Tagger)			Zuordnung von Wörtern und Satzzeichen zu Wortarten, z.B. "sagt := Verb in 3.Person"
4) Lemmatizer						Bilden von Grundformen, z.B. "sagt -> sagen"
5) NER ("Named Entity Recognizer")			Eigennamenereknnung (auch Orte, Organisationen,...)
6) Parser						Erschliessung der Semnatik, z.B. 	"Karl Müller kauft wie immer an einem schönen Tag ein Eis und isst es -> Karl kauft Eis/ Karl isst Eis"
7) Writer						
*/

public class Pipeline {

	public static void runPipelineImpl(final CollectionReaderDescription readerDesc,
								   final AnalysisEngineDescription... descs) throws UIMAException, IOException {
		ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();

		// Create the components
		final CollectionReader reader = UIMAFramework.produceCollectionReader(readerDesc, resMgr, null);

		// Create AAE
		final AnalysisEngineDescription aaeDesc = createEngineDescription(descs);

		// Instantiate AAE
		final AnalysisEngine aae = UIMAFramework.produceAnalysisEngine(aaeDesc, resMgr, null);

		// Create CAS from merged metadata

		final CAS cas = CasCreationUtils.createCas(asList(reader.getMetaData(), aae.getMetaData()));
		reader.typeSystemInit(cas.getTypeSystem());
		try {
			// Process
			while (reader.hasNext()) {
				reader.getNext(cas);
				aae.process(cas);
				cas.reset();
			}

			// Signal end of processing
			aae.collectionProcessComplete();
		} finally {
			// Destroy
			aae.destroy();
		}
	}

	public static void run() throws Exception {

		runPipelineImpl(
			createReaderDescription(TextReader.class,        //1)
				TextReader.PARAM_SOURCE_LOCATION,
				System.getProperty("java.io.tmpdir") + "/tmp-news.txt",
				TextReader.PARAM_LANGUAGE, "de"),
			createEngineDescription(OpenNlpSegmenter.class),    //2)
			createEngineDescription(OpenNlpPosTagger.class),    //3)
			createEngineDescription(LanguageToolLemmatizer.class),    //4)
			createEngineDescription(StanfordNamedEntityRecognizer.class),    //5)
			createEngineDescription(MaltParser.class),        //6
			createEngineDescription(XmiWriter.class,        //7)
				XmiWriter.PARAM_TARGET_LOCATION, System.getProperty("java.io.tmpdir") + "/tmp-annotated.xmi"));
	}
}
