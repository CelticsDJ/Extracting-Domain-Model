package utils;

import java.util.*;

import gate.annotation.AnnotationSetImpl;
import gate.stanford.DependencyRelation;
import org.apache.commons.lang3.math.NumberUtils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;
import gate.util.InvalidOffsetException;
import data.Concept_Class;
import data.Association_Relation;

public class Utilities {

	//Get NP_id
	public static int getMapped_NP(Document doc, int token_id)
	{
		/*AnnotationSet annotations = doc.getAnnotations();

		AnnotationSet NPs = new AnnotationSetImpl(doc);

		for(Annotation a: annotations) {

			if(a.getType().equals("SyntaxTreeNode") && a.getFeatures().get("cat").equals("NP")) {
				NPs.add(a);
			}
		}*/

		Annotation token = doc.getAnnotations().get(token_id);

		//token = convertNNId(doc, token);

		AnnotationSet NPs = gate.Utils.getCoveringAnnotations(doc.getAnnotations(), token, "Parse_NP");
		//NPs = gate.Utils.getCoveringAnnotations(NPs, token, "SyntaxTreeNode");
		if(NPs.size() == 1){
			Annotation NP = gate.Utils.getOnlyAnn(NPs);
			return NP.getId();
		}			
		return token_id;
	}
	
	public static int getMapped_VP(Document doc, int token_id)
	{
		Annotation token = doc.getAnnotations().get(token_id);		
		AnnotationSet VPs = gate.Utils.getCoextensiveAnnotations(doc.getAnnotations(), token, "Relation_Verb");
		if(VPs.size() == 1){
			Annotation VP = gate.Utils.getOnlyAnn(VPs);
			return VP.getId();
		}			
		return 0;
	}
	
	public static Concept_Class getMapped_NPPrunedString(Document doc, int token_id)
	{
		if(token_id == 0)
			return new Concept_Class("", 0, "");
		
		Annotation token = doc.getAnnotations().get(token_id);
		
		AnnotationSet NPs = gate.Utils.getCoveringAnnotations(doc.getAnnotations(), token, "Parse_NP");
		if(NPs.size() == 1){
			Annotation NP = gate.Utils.getOnlyAnn(NPs);
			String cardinality = cardinalitiesRules(doc, NP);
			Concept_Class cl = new Concept_Class(NP.getFeatures().get("pruned_string").toString(), NP.getId(), cardinality);			
			return cl;			
		}			
		return new Concept_Class(gate.Utils.stringFor(doc, doc.getAnnotations().get(token_id)), token_id, "1");
	}
	
	public static Concept_Class getNextNPinSentence(Document doc, int token_id)
	{
		if(token_id == 0)
			return new Concept_Class("", 0, "");
		Annotation token = doc.getAnnotations().get(token_id);
		AnnotationSet sentences = Utils.getOverlappingAnnotations(doc.getAnnotations(), token, "Sentence");	
		AnnotationSet NPs = Utils.getContainedAnnotations(doc.getAnnotations(), sentences, "Parse_NP");
		List<Annotation> NPs_List = Utils.inDocumentOrder(NPs);
		for(Annotation NP: NPs_List)
		{
			if(Utils.end(token) < Utils.start(NP))
			{
				String cardinality = cardinalitiesRules(doc, NP);
				Concept_Class cl = new Concept_Class(NP.getFeatures().get("pruned_string").toString(), NP.getId(), cardinality);
				return cl;
			}
		}
		return new Concept_Class(gate.Utils.stringFor(doc, doc.getAnnotations().get(token_id)), token_id, "1");
	}

	//bug : 有时会得到输入的source为输出chain的target
	public static List<Annotation> isChainSource(Document doc, int annot_ID)
	{
		AnnotationSet inputAS = doc.getAnnotations();
		Annotation possibleSrc = inputAS.get(annot_ID);
		List<Annotation> Chain_1 = inputAS.get("Chain_1").inDocumentOrder();
		List<Annotation> list_chains = new ArrayList<>();
		for(Annotation a : Chain_1) {
			if (a.getFeatures().get("source_ID").equals(possibleSrc.getId())) {
				list_chains.add(a);
			}
		}
		//AnnotationSet chains = Utils.getOverlappingAnnotations(inputAS, possibleSrc, "Chain_1");
		//List<Annotation> list_chains = Utils.inDocumentOrder(chains);
		List<Annotation> return_chains = new ArrayList<Annotation>();
		for(Annotation chain: list_chains)
		{
			/* if(chain.getFeatures().get("source_ID").toString().equals(Integer.toString(annot_ID)))
			{
				return_chains.add(chain);
			}*/
			return_chains.add(chain);
		}
		return return_chains;
	}
	
	public static void addAnnotation(Document doc, Annotation start_annot, Annotation end_annot, FeatureMap features, String Annotation_Name)
	{
		AnnotationSet inputAS = doc.getAnnotations();
		Long start = gate.Utils.start(start_annot);
		Long end = gate.Utils.end(end_annot);		
		try {
			if(start < end)
			{				
				features.put("string", prunedStringforSegment(doc, start, end));				
			}
			else
			{		
					start = gate.Utils.start(end_annot);
					end = gate.Utils.end(start_annot);
					features.put("string", prunedStringforSegment(doc, start, end));	
			}
			inputAS.add(start, end, Annotation_Name, features);
		} catch (InvalidOffsetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static StringTuple getRelation_Target(String str)
	{
		String [] pairs = str.split("###");
		if(pairs.length == 2)
		{
			return new StringTuple(pairs[0].replace(pairs[1], ""), pairs[1]);
		}
		return new StringTuple("", str);
	}
	
	public static Association_Relation formRelations(String src, String trgt, String src_cardinality, String target_cardinality, String rel, boolean isXcomp, String baseRule)
	{
		Association_Relation relation = null;
		if(isXcomp)
		{
			relation = new Association_Relation(new Concept_Class(src, 0, src_cardinality), new Concept_Class(trgt, 0, target_cardinality), rel, "N2");
		}
		else
		{
			relation = new Association_Relation(new Concept_Class(src, 0, src_cardinality), new Concept_Class(trgt, 0, target_cardinality), rel, baseRule);
		}
		return relation;
	}
	
	public static boolean IstheRelationMapping(Document doc, Annotation annot, String mappingAnnot)
	{
		AnnotationSet inputAS = doc.getAnnotations();		
		AnnotationSet mappingAnnots = Utils.getOverlappingAnnotations(inputAS, annot, mappingAnnot);		
		if(mappingAnnots.size() > 0)
			return true;
		
		return false;
	}
	
	public static int getRCMOD_NP(Document doc, Annotation VP)
	{
		AnnotationSet inputAS = doc.getAnnotations();		
		AnnotationSet mappingAnnots = Utils.getOverlappingAnnotations(inputAS, VP, "Dep-rcmod");
		List<Annotation> list_mappingAnnots = Utils.inDocumentOrder(mappingAnnots);
		int token_id = Integer.parseInt(list_mappingAnnots.get(0).getFeatures().get("token1_Id").toString());
		
		return getMapped_NP(doc, token_id);
	}
	
	private static String prunedStringforSegment(Document doc, Long start, Long end)
	{
		AnnotationSet NPs = doc.getAnnotations().get("Parse_NP", start, end);
		String originalStr = Utils.stringFor(doc, start, end);
		for(Annotation NP: NPs)
		{
			 originalStr = originalStr.replace(Utils.stringFor(doc, Utils.start(NP), Utils.end(NP)), NP.getFeatures().get("pruned_string").toString());
		}
		return originalStr;
	}
	
	private static String cardinalitiesRules(Document doc, Annotation NP)
	{
		FeatureMap features = NP.getFeatures();
		String [] universalQuantifiers = {"a", "an", "all", "some"};
		String [] singleQuantifiers = {"the"};
		String token = features.get("firstToken").toString().toLowerCase();
		if(features.get("isPlural").toString().equals("true"))
		{
			return "*";
		}
		
		if(features.get("isPlural").toString().equals("false") && !token.equals(""))
		{
			if(NumberUtils.isNumber(token))
			{
				return token;
			}
			
			if(Arrays.asList(universalQuantifiers).contains(token))
			{
				return "*";
			}
			
			if(Arrays.asList(singleQuantifiers).contains(token))
			{
				return "1";
			}
		}
		return "1";
	}

	public static String getRelationType(Document doc, DependencyRelation rel) {

		Annotation targ = doc.getAnnotations().get(rel.getTargetId());
		for(DependencyRelation dep : (List<DependencyRelation>)targ.getFeatures().get("dependencies")) {

			if(dep.getType().equals("case")) {
				return doc.getAnnotations().get(dep.getTargetId()).getFeatures().get("string").toString();
			}
		}

		return "";
	}

	public static Concept_Class getRealTarget(Document doc, Annotation VP, int token_id) {
		if(token_id == 0)
			return new Concept_Class("", 0, "");

		AnnotationSet tokens = doc.getAnnotations().get("Token");

		AnnotationSet VBs = gate.Utils.getOverlappingAnnotations(tokens, VP);


		Integer VB_id = 0;
		if(VBs.size() == 0) {
			return new Concept_Class(gate.Utils.stringFor(doc, doc.getAnnotations().get(token_id)), token_id, "1");
		}
		else {
			for(Annotation VB : VBs) {
				//没有dependencies的会报错
				List<DependencyRelation> dependencies = (List<DependencyRelation>) VB.getFeatures().get("dependencies");
				if(dependencies != null) {
					for (DependencyRelation dep : dependencies) {
						if (dep.getType().equals("nmod")) {
							VB_id = VB.getId();
						}
					}
				}
			}
		}


		int return_id = token_id;

		while(return_id >= 0) {

			Annotation tmp = tokens.get(return_id);

			if(tmp == null) {
				break;
			}

			//没有dependencies的会报错
			List<DependencyRelation> dependencies = (List<DependencyRelation>)tmp.getFeatures().get("dependencies");

			if (dependencies != null) {
				for(DependencyRelation dr : dependencies) {
					//应该是equals.(VP_id) 暂时不好改
					if(dr.getType().equals("acl:relcl") && dr.getTargetId().equals(VB_id)) {
						return new Concept_Class(gate.Utils.stringFor(doc, doc.getAnnotations().get(return_id)), return_id, "1");
					}
				}
			}

			/*if(tmp.getFeatures().get("category").toString().startsWith("NN")) {
				return new Concept_Class(gate.Utils.stringFor(doc, doc.getAnnotations().get(return_id)), return_id, "1");
			}*/

			return_id -= 2;
		}

		return new Concept_Class(gate.Utils.stringFor(doc, doc.getAnnotations().get(token_id)), token_id, "1");
	}

	public static List<Integer> getTokenID(Document doc, int NP_id)
	{
		Annotation NP = doc.getAnnotations().get(NP_id);

		AnnotationSet tokens = gate.Utils.getOverlappingAnnotations(doc.getAnnotations(), NP, "Token");
		//NPs = gate.Utils.getCoveringAnnotations(NPs, token, "SyntaxTreeNode");

		List<Integer> return_list = new ArrayList<>();
		for(Annotation a : tokens) {
			return_list.add(a.getId());
		}

		Collections.sort(return_list);

		return return_list;
	}

	public static List<Integer> getOverlappingNPs(Document doc, int NP_id) {
		Annotation NP = doc.getAnnotations().get(NP_id);

		AnnotationSet Parse_NPs = gate.Utils.getOverlappingAnnotations(doc.getAnnotations(), NP, "Parse_NP");

		List<Integer> return_list = new ArrayList<>();
		for(Annotation a : Parse_NPs) {
			return_list.add(a.getId());
		}

		Collections.sort(return_list);

		return return_list;
	}

	/*private static Annotation convertNNId(Document doc, Annotation a) {

		AnnotationSet annotations = doc.getAnnotations();

		ArrayList<Annotation> STNs = new ArrayList<>();
		ArrayList<Annotation> STN_NNs = new ArrayList<>();

		//Get SyntaxTreeNodes
		for(Annotation stn : annotations) {
			if(stn.getType().equals("SyntaxTreeNode")) {
				STNs.add(stn);
			}
		}

		//Get NNs from SyntaxTreeNodes
		for(Annotation stn : STNs) {
			if(stn.getFeatures().get("cat").equals("NN")) {
				STN_NNs.add(stn);
			}
		}

		for(Annotation stn : STN_NNs){
			if(stn.getStartNode().equals(a.getStartNode()) && stn.getEndNode().equals(a.getEndNode())) {
				return stn;
			}
		}

		return a;
	}*/

}
