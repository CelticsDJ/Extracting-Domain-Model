package Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import data.Write_To_Excel;
import gate.*;
import gate.util.InvalidOffsetException;

import data.DOT_Graphviz_conversion;
import data.GlobalVariables;
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

		for ( int i = 1; i <= 2; ++i) {
			Corpus corpus = init(i);
			for (Document annoted_Doc : corpus) {

				GlobalVariables.setAnnotatedDoc(annoted_Doc);

				DeriveAnnotations.DeriveAnnotations();

				extractInfoFromAnnotatedDoc(i);

			}
		}
	}

	private static Corpus init(int i) throws Exception
	{
		Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.setName("Test_Corpus");

		URL docURL = new URL("file:///C:\\Users\\26309\\workspace\\Extracting-Domain-Model\\resources\\All_Annotations_OpenCossReqs.xml");

		if(i == 2) {
			docURL = new URL("file:///C:\\Users\\26309\\workspace\\Extracting-Domain-Model\\resources/iTrust.xml");
		}

		Document doc = Factory.newDocument(docURL, "UTF-8");
		doc.setName("OpenCossReqs");

	    corpus.add(doc);

		return corpus;
	}
	
	private static void extractInfoFromAnnotatedDoc(int i) throws InvalidOffsetException, SecurityException, IOException
	{				
		Relation_Rules.extractRelations();
		Classes_Rules.classesInfo();
		ExtractRelations_includingChains.traverseRelations();
        Concept_Pattern.Concept_Pattern();

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
