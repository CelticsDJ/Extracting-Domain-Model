package Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.SwingUtilities;

import edu.stanford.nlp.util.ArraySet;
import gate.*;
import gate.creole.ANNIEConstants;
import gate.creole.ConditionalSerialAnalyserController;
import gate.gui.MainFrame;
import gate.util.InvalidOffsetException;
import gate.util.persistence.PersistenceManager;
import data.DOT_Graphviz_conversion;
import data.GlobalVariables;
import data.JSONConversion;
import processing.ExtractRelations_includingChains;
import rules.Classes_Rules;
import rules.Relation_Rules;
import utils.DeriveAnnotations;

public class Model_Extraction_Main {
	
	public static void main(String [] args) throws Exception
	{				
		Corpus corpus = init();
		for(Document annoted_Doc : corpus)
		{

			GlobalVariables.setAnnotatedDoc(annoted_Doc);

			DeriveAnnotations.DeriveAnnotations();

			extractInfoFromAnnotatedDoc();

			//extractAtomicNPs();
		}
	}
	
	/*
	 * This method initializes all the gate resources and is used to execute the pipeline (defined within this method)
	 * on the document (defined within this method)
	 * It returns the annotated document
	 */
	private static Corpus init() throws Exception
	{
		//Set GATE home to right location 
		File file = new File("/Applications/GATE_Developer_8.6/");
		Gate.setGateHome(file);
		Gate.setSiteConfigFile(new File(file.getPath() + "/gate.xml"));

		URL docURL_1 = new URL("file:///Users/dujianuo/Desktop/domain model/Extracting Domain Model/resources/All_Annotations_OpenCossReqs.xml");
		//URL docURL_1 = new URL("file:///Users/dujianuo/Desktop/domain model/Extracting Domain Model/resources/Partial_Annotations_OpenCossReqs.xml");
		
		//prepare the GATE library
		Gate.init();
		
		//Show the GATE developer window
		/*SwingUtilities.invokeAndWait(new Runnable() { public void run() {
			MainFrame.getInstance().setVisible(true); }
		});*/
		
		//Load Creole Plugin;
		//System.out.println(Gate.getPluginsHome().getAbsolutePath());
		//Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR).toURI().toURL());
		//Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), "OpenNLP").toURI().toURL());
		//Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), "Stanford_CoreNLP").toURI().toURL());
		//Gate.getCreoleRegister().registerDirectories(new File(gate.Gate.getPluginsHome(), "Tools").toURI().toURL());
				
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
		
		//ConditionalSerialAnalyserController pipeline = (ConditionalSerialAnalyserController) PersistenceManager.loadObjectFromFile(new File("/Users/dujianuo/Desktop/domain model/Extracting Domain Model/models.gapp"));
		//CorpusController pipeline = (CorpusController) PersistenceManager.loadObjectFromFile(new File("/Users/dujianuo/Desktop/domain model/Extracting Domain Model/models.gapp"));
		//pipeline.setCorpus(corpus);
		//pipeline.execute();
		
		return corpus;
	}
	
	private static void extractInfoFromAnnotatedDoc() throws InvalidOffsetException, SecurityException, IOException
	{				
		Relation_Rules.extractRelations();
		
		Classes_Rules.classesInfo();
		
		ExtractRelations_includingChains.traverseRelations();
		
		//Classes_Rules.printAdjNPs(doc);
		
		String results = JSONConversion.converttoJSON();
		
		DOT_Graphviz_conversion.writeDOTFile("/Users/dujianuo/Desktop/domain model/Extracting Domain Model/result/example_1.dot");
		//DOT_Graphviz_conversion.writeDOTFile("/Users/dujianuo/Desktop/domain model/Extracting Domain Model/result/example_2.dot");

		System.out.println("*******************  RESULTS *************");
		System.out.println(results);
	}

	/*private static void extractAtomicNPs() {
		Document annotateDoc = GlobalVariables.annotated_doc;
		AnnotationSet NPs = annotateDoc.getAnnotations().get("SyntaxTreeNode");
		ArraySet<Annotation> atomicNPs = new ArraySet<>();
		for (Annotation a : NPs) {
			if (a.getFeatures().get("cat").equals("NP") && isAtomic(a)){
				atomicNPs.add(a);
			}
		}
		System.out.println(atomicNPs.size());
	}

	private static boolean isAtomic(Annotation a) {

		Document annotateDoc = GlobalVariables.annotated_doc;
		AnnotationSet NPs = annotateDoc.getAnnotations().get("SyntaxTreeNode");

		List<Integer> consists = (List<Integer>) a.getFeatures().get("consists");
		for (Integer i : consists) {
			for (Annotation tmp : NPs) {
				if (tmp.getId() == i && tmp.getFeatures().get("cat").equals("NP")) {
					return false;
				}
			}
		}

		return true;
	}*/


}
