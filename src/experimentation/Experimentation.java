package experimentation;

import java.io.File;
import java.net.URL;
import java.util.Date;

import javax.swing.SwingUtilities;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.SimpleDocument;
import gate.creole.ANNIEConstants;
import gate.creole.ConditionalSerialAnalyserController;
import gate.gui.MainFrame;
import gate.util.persistence.PersistenceManager;

public class Experimentation {
	
	public static void main(String [] args) throws Exception
	{
		
		init();
		
		//Experiment write a document
		FeatureMap params = Factory.newFeatureMap();
		params.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, "This is a document!");
		FeatureMap feats = Factory.newFeatureMap();
		feats.put("CreatedBy", "CA");
		Factory.createResource("gate.corpora.DocumentImpl", params, feats, "My first Document");
		
		//Experiment Load a document
		//Document doc = Factory.newDocument("ReqsDoc");
 		Document doc = Factory.newDocument(new URL("file:///Users/chetan.arora/Desktop/GHOST_Reqs.txt"), "UTF-8");
		doc.setName("GHOST_Reqs");
		FeatureMap doc_params = Factory.newFeatureMap();
		doc_params.put("date", (new Date()).toString());
		doc.setFeatures(doc_params);
	}
	
	private static Document init() throws Exception
	{
		//Set GATE home to right location 
		File file = new File("/Applications/GATE_Developer_8.1/");
		Gate.setGateHome(file);
		Gate.setSiteConfigFile(new File(file.getPath() + "/gate.xml"));
		
		//prepare the GATE library
		Gate.init();
		
		//Show the GATE developer window
		SwingUtilities.invokeAndWait(new Runnable() { public void run() {
			MainFrame.getInstance().setVisible(true); }
		});
		
		//Load Creole Plugins
		Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR).toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), "OpenNLP").toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), "Stanford_CoreNLP").toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), "Tools").toURI().toURL());
		
		
		Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.setName("Test_Corpus");
		
	    //URL docURL = NP_Pipeline.class.getResource("/resources/SES_M&C_Reqs_final.txt");
		URL docURL = new URL("file:////Users/chetan.arora/Desktop/Model Extraction Files/Doc/GDDN_Sample.txt");
		//URL docURL = NPPipeline_KeywordsExtraction.class.getResource("/resources/ExampleReqs.txt");
		
		FeatureMap params1 = Factory.newFeatureMap();
		params1.put(SimpleDocument.DOCUMENT_URL_PARAMETER_NAME, docURL);
		params1.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8");
		
		FeatureMap featMap = Factory.newFeatureMap();
		//featMap.put("date", new Date());
		
		Document doc = Factory.newDocument(docURL, "UTF-8");
		//Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params1, featMap, "document");
		doc.setName("GDDN_SampleDocument");
	    		
	    //corpus.add(document);
	    corpus.add(doc);
			    			
		//ProcessingResource pr =  (ProcessingResource) Factory.createResource("gate.stanford.Tagger", Factory.newFeatureMap());
		
		ConditionalSerialAnalyserController pipeline = (ConditionalSerialAnalyserController) PersistenceManager.loadObjectFromFile(new File("/Users/chetan.arora/Desktop/Model Extraction Files/models.gapp"));
		pipeline.setCorpus(corpus);
		pipeline.execute();
		
		return doc;
	}

}
