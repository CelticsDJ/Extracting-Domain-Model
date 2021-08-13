package Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.SwingUtilities;

import data.Write_To_Excel;
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

		Corpus testCorpus = init("/Users/dujianuo/Desktop/Extracting-Domain-Model/resources/requirements.txt");

		GlobalVariables.setAnnotatedDoc(testCorpus.get(0));

		DeriveAnnotations.DeriveAnnotations();

		extractInfoFromAnnotatedDoc();

		for (String arg : args) {
			Corpus corpus = init(arg);
			for (Document annoted_Doc : corpus) {

				GlobalVariables.setAnnotatedDoc(annoted_Doc);

				DeriveAnnotations.DeriveAnnotations();

				extractInfoFromAnnotatedDoc();

			}
		}
	}
	
	/*
	 * This method initializes all the gate resources and is used to execute the pipeline (defined within this method)
	 * on the document (defined within this method)
	 * It returns the annotated document
	 */
	private static Corpus init(String arg) throws Exception
	{
		Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.setName("Test_Corpus");

		URL docURL = new URL("file://" + arg);

		Document doc = Factory.newDocument(docURL, "UTF-8");
		doc.setName("OpenCossReqs");

	    corpus.add(doc);

	    ConditionalSerialAnalyserController controller = (ConditionalSerialAnalyserController) PersistenceManager.loadObjectFromFile(new File("resources/model8.6.gapp"));

	    controller.setCorpus(corpus);
	    controller.execute();

	    return controller.getCorpus();

		//return corpus;
	}
	
	private static void extractInfoFromAnnotatedDoc() throws InvalidOffsetException, SecurityException, IOException
	{				
		Relation_Rules.extractRelations();
		
		Classes_Rules.classesInfo();
		
		ExtractRelations_includingChains.traverseRelations();

        Concept_Pattern.Concept_Pattern();
		
		//Classes_Rules.printAdjNPs(doc);
		
		String results = JSONConversion.converttoJSON();
		
		//DOT_Graphviz_conversion.writeDOTFile("/Users/dujianuo/Desktop/domain model/Extracting Domain Model/result/example_1.dot");
		/*
		if(i == 1) {
			DOT_Graphviz_conversion.writeDOTFile("result/Partial_Annotations_OpenCossReqs.dot");
		}
		else if (i == 2) {
			DOT_Graphviz_conversion.writeDOTFile("result/iTrust.dot");
			Write_To_Excel.Write_to_Excel("result/iTrust.xls");
		}
		else if (i == 3) {
			DOT_Graphviz_conversion.writeDOTFile("result/ATM_Example.dot");
		}
		 */
		String filename = "result/".concat(GlobalVariables.annotated_doc.getName());
		DOT_Graphviz_conversion.writeDOTFile(filename.concat(".dot"));
		Write_To_Excel.Write_to_Excel(filename.concat(".xls"));

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
