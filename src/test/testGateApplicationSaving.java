package test;

import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.SimpleDocument;
import gate.creole.ANNIEConstants;
import gate.creole.ConditionalSerialAnalyserController;
import gate.gui.MainFrame;
import gate.util.persistence.PersistenceManager;

public class testGateApplicationSaving {
	
	
	public static void main(String [] args) throws Exception
	{				
		Corpus corpus = init();
		
	}
	
	
	/*
	 * This method initializes all the gate resources and is used to execute the pipeline (defined within this method)
	 * on the document (defined within this method)
	 * It returns the annotated document
	 */
	private static Corpus init() throws Exception
	{
		//Set GATE home to right location 
		File file = new File("/Applications/GATE_Developer_8.1/");
		Gate.setGateHome(file);
		Gate.setSiteConfigFile(new File(file.getPath() + "/gate.xml"));

		URL docURL_1 = new URL("file:///Users/dujianuo/Desktop/domain model/redomex/requirements.txt");
		//URL docURL_1 = new URL("file:///Users/chetan.arora/Dropbox/PhD Folder/Useful_Code/Model Extraction Files/Doc/GSTSIMReqs.txt");
		//URL docURL_1 = new URL("file:///Users/chetan.arora/Dropbox/PhD Folder/Useful_Code/Model Extraction Files/Doc/Single_Req.txt");
		//URL docURL_2 = new URL("file:///Users/chetan.arora/Dropbox/PhD Folder/Useful_Code/Model Extraction Files/Doc/OpenCossReqs.txt");
		
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
		
		FeatureMap params1 = Factory.newFeatureMap();
		params1.put(SimpleDocument.DOCUMENT_URL_PARAMETER_NAME, docURL_1);
		params1.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8");
		
		//FeatureMap featMap = Factory.newFeatureMap();
		//featMap.put("date", new Date());
		
		Document doc_1 = Factory.newDocument(docURL_1, "UTF-8");
		//Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params1, featMap, "document");
		doc_1.setName("OpenCossReqs");
		//Document doc_2 = Factory.newDocument(docURL_2, "UTF-8");
		//Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params1, featMap, "document");
		//doc_2.setName("Mix_Doc");
	    		
	    //corpus.add(document);
	    corpus.add(doc_1);
	    //corpus.add(doc_2);
			    			
		//ProcessingResource pr =  (ProcessingResource) Factory.createResource("gate.stanford.Tagger", Factory.newFeatureMap());
		
		ConditionalSerialAnalyserController pipeline = (ConditionalSerialAnalyserController) PersistenceManager.loadObjectFromFile(new File("/Users/dujianuo/Desktop/domain model/My_extraction/models.gapp"));
		ProcessingResource pr = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", Factory.newFeatureMap());
		pipeline.add(pr);
		PersistenceManager.saveObjectToFile((Object)pipeline, new File("/Users/dujianuo/Desktop/savedApp.gapp"), true, false);
		pipeline.setCorpus(corpus);
		pipeline.execute();
		
		return corpus;
	}

}
