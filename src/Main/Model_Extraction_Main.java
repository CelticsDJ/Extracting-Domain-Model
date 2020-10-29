package Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.SwingUtilities;

import gate.*;
import gate.creole.ANNIEConstants;
import gate.creole.ConditionalSerialAnalyserController;
import gate.gui.MainFrame;
import gate.util.InvalidOffsetException;
import gate.util.persistence.PersistenceManager;
import data.DOT_Graphviz_conversion;
import data.GlobalVariables;
import data.JSONConversion;
import processing.Concept_Pattern;
import processing.ExtractRelations_includingChains;
import rules.Classes_Rules;
import rules.Relation_Rules;
import utils.DeriveAnnotations;

public class Model_Extraction_Main {
	
	public static void main(String [] args) throws Exception
	{
		//Set GATE home to right location
		File file = new File("/Applications/GATE_Developer_8.6/");

		Gate.setGateHome(file);
		Gate.setSiteConfigFile(new File(file.getPath() + "/gate.xml"));

		//prepare the GATE library
		Gate.init();

		for ( int i = 2; i <= 2; ++i) {
			Corpus corpus = init(i);
			for (Document annoted_Doc : corpus) {

				GlobalVariables.setAnnotatedDoc(annoted_Doc);

				DeriveAnnotations.DeriveAnnotations();

				extractInfoFromAnnotatedDoc(i);

			}
		}
	}
	
	/*
	 * This method initializes all the gate resources and is used to execute the pipeline (defined within this method)
	 * on the document (defined within this method)
	 * It returns the annotated document
	 */
	private static Corpus init(int i) throws Exception
	{
		Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.setName("Test_Corpus");

		URL docURL_1 = new URL("file:///C:\\Users\\26309\\workspace\\Extracting-Domain-Model\\resources\\All_Annotations_OpenCossReqs.xml");

		if(i == 2) {
			docURL_1 = new URL("file:///C:\\Users\\26309\\workspace\\Extracting-Domain-Model\\resources/iTrust.xml");
		}
		else if(i == 3) {
			docURL_1 = new URL("file:///C:\\Users\\26309\\workspace\\Extracting-Domain-Model\\resources/ATM_Example.xml");
		}
		Document doc_1 = Factory.newDocument(docURL_1, "UTF-8");
		doc_1.setName("OpenCossReqs");

	    corpus.add(doc_1);

		return corpus;
	}
	
	private static void extractInfoFromAnnotatedDoc(int i) throws InvalidOffsetException, SecurityException, IOException
	{				
		Relation_Rules.extractRelations();
		
		Classes_Rules.classesInfo();
		
		ExtractRelations_includingChains.traverseRelations();

        Concept_Pattern.Concept_Pattern();
		
		//Classes_Rules.printAdjNPs(doc);
		
		String results = JSONConversion.converttoJSON();
		
		//DOT_Graphviz_conversion.writeDOTFile("/Users/dujianuo/Desktop/domain model/Extracting Domain Model/result/example_1.dot");
		if(i == 1) {
			DOT_Graphviz_conversion.writeDOTFile("result/Partial_Annotations_OpenCossReqs.dot");
		}
		else if (i == 2) {
			DOT_Graphviz_conversion.writeDOTFile("result/iTrust.dot");
		}
		else if (i == 3) {
			DOT_Graphviz_conversion.writeDOTFile("result/ATM_Example.dot");
		}

		//System.out.println("*******************  RESULTS *************");
		//System.out.println(results);
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
