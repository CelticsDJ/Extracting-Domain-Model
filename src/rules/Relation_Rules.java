package rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
//import gate.stanford.DependencyRelation;
import gate.util.InvalidOffsetException;
import gate.stanford.DependencyRelation;
import data.Concept_Class;
import data.GlobalVariables;
import utils.Utilities;

import static utils.Utilities.getRelationType;

public class Relation_Rules {
	
	static HashMap<Integer, String> VPsMap = new HashMap<Integer, String>();
	static boolean isAdvMod = false;
	
	/*
	 * Tasks - Prepare VP_NP Chains which connect VPs to NPs by prepositions
	 */
	/*
	 * Maybe VP should be VB
	 */
	public static void extractRelations() throws InvalidOffsetException
	{
		Document annotatedDoc = GlobalVariables.annotated_doc;
		AnnotationSet Relation_Verb = annotatedDoc.getAnnotations().get("Relation_Verb");
		List<Annotation> VPs = Utils.inDocumentOrder(Relation_Verb);
		/*ArraySet<Annotation> VPs = new ArraySet<>();// = annotatedDoc.getAnnotations().get("SyntaxTreeNode");

		/*for (Annotation a : VPs) {
			if(!a.getFeatures().get("cat").equals("VP")){
				VPs.remove(a);
			}
		}
		for (Annotation a : tmp) {
			if (a.getType().equals("Token")) {
				if(a.getFeatures().get("category").equals("VB") && a.getFeatures().get("dependencies") != null){
					VPs.add(a);
				}
			}
		}*/
				
		for(Annotation VP: VPs)
		{
			if(VP.getFeatures().get("category").toString().equals("JJR")) {
				continue;
			}

			int num_Objects = 0;
			isAdvMod = false;
			String relVerb = Utils.cleanStringFor(annotatedDoc, VP);
			//String VPStr = getVPString(annotatedDoc, VP);
			String VPStr = VP.getFeatures().get("string").toString();
			
			FeatureMap VP_features = VP.getFeatures();
			FeatureMap updatedFeatures = Factory.newFeatureMap();

			/*
			 *	VP_features.get("dependencies") need to be converted from List<String> to List<DependencyRelation>
			 */
			/*List<String> dependencies = (List<String>) VP_features.get("dependencies");
			List<DependencyRelation> list_dependencies = new ArrayList<>();
			for (String dep : dependencies) {
				list_dependencies.add(new DependencyRelation(dep));
			}*/

			//@SuppressWarnings("unchecked")
			List<DependencyRelation> list_dependencies = (List<DependencyRelation>) VP_features.get("dependencies");
			
			for(DependencyRelation rel: list_dependencies)
			{
				//System.out.println(rel.getType() + " -- " + rel.getTargetId());
				if(rel.getType().equals("nsubj") || rel.getType().equals("xsubj"))
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("Subject", Subject_Id);
				}
				else if(rel.getType().equals("xsubj"))
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("xSubject", Subject_Id);
				}
				else if(rel.getType().equals("dobj"))
				{					
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					if(Subject_Id != 0)
					{
						num_Objects++;
						updatedFeatures.put("D_Object_" + num_Objects, Subject_Id);
					}					
				}
				else if(rel.getType().equals("iobj"))
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("I_Object", Subject_Id);
				}
				else if(rel.getType().equals("pobj"))//Only in prep
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("P_Object", Subject_Id);
				}				
				else if(rel.getType().equals("nsubjpass"))
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("Passive_Subject", Subject_Id);
				}
				else if(rel.getType().equals("agent"))
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("Agent", Subject_Id);
				}
				else if(rel.getType().contains("prep_")) // not used
				{
					updatedFeatures.put(rel.getType(), rel.getTargetId());					
					makeVP_PPChains(annotatedDoc, VP, rel);
				}
				else if(rel.getType().equals("nmod"))
				{
					updatedFeatures.put(rel.getType(), rel.getTargetId());
					makeVP_PPChains(annotatedDoc, VP, rel);
				}
				else if(isAdvMod)
				{
					updatedFeatures.put("isAdvMod", 1);										
				}
				else if(rel.getType().equals("xcomp")) //Forward Dependency XCOMP is always a verb to verb dependency -- Merge the two verbs as a single relation
				{
					int verb_Id = Utilities.getMapped_VP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("FD_xcomp", verb_Id);
				}
				else if(rel.getType().equals("ccomp")) //Forward Dependency CCOMP is always a verb to verb dependency -- Merge the two verbs as a single relation
				{
					int verb_Id = Utilities.getMapped_VP(annotatedDoc, rel.getTargetId());
					updatedFeatures.put("FD_ccomp", verb_Id);
				}
			}
			
			updatedFeatures.put("Num_Objects" , num_Objects);
			updatedFeatures.put("str" , VPStr);
			updatedFeatures.put("root", VPStr.replace(relVerb, VP_features.get("root").toString())); //Updated this to add adverb to the root
			annotatedDoc.getAnnotations().add(Utils.start(VP), Utils.end(VP), "Relations", updatedFeatures);			
		}
	}

	/*private static void makeVP_PPChains(Document annotatedDoc, Annotation VP, DependencyRelation rel)
	{
		AnnotationSet inputAS = annotatedDoc.getAnnotations();
		
		FeatureMap features = Factory.newFeatureMap();
		String VP1 = VP.getFeatures().get("text").toString();

		String relation = rel.getType().replaceAll("prep(c)?_", "");
		//String relation = rel.getType().replaceAll("prep(c)?", "");
		//String relation = inputAS.get(rel.getTargetId()).getFeatures().get("text").toString();
		Concept_Class target_cl;
		String target;
		if(rel.getType().contains("prepc"))
		{
			target_cl = Utilities.getNextNPinSentence(annotatedDoc, rel.getTargetId());
			target = target_cl.getName();
		}
		else
		{
			target_cl = Utilities.getMapped_NPPrunedString(annotatedDoc, rel.getTargetId());
			target = target_cl.getName();
		}
		
		features.put("string", VP1 + " " + relation + " " + target);
		//features.put("string", VP1 + " " + relation + " " + inputAS.get(rel.getTargetId()).getFeatures().get("text"));
		features.put("source_ID", VP.getId());
		features.put("source_Type", "VP");
		features.put("source_String", VP1);

		features.put("target_ID", target_cl.getID());
		features.put("target_Type", "Parse_NP");
		features.put("target_String", target);
		features.put("relation_Type", rel.getType());					
		features.put("kind", "VP_NP");
		features.put("cardinality", target_cl.getCardinality());
		if(!(VP1.equals("provide")))
		{
			Utilities.addAnnotation(annotatedDoc, VP, inputAS.get(target_cl.getID()), features, "Chain_1");
		}
	}*/

	private static void makeVP_PPChains(Document annotatedDoc, Annotation VP, DependencyRelation rel)
	{
		AnnotationSet inputAS = annotatedDoc.getAnnotations();

		FeatureMap features = Factory.newFeatureMap();
		String VP1 = VP.getFeatures().get("string").toString();
		String relation;

		try {
			relation = getRelationType(annotatedDoc, rel).replace("according", "according to");
		}catch (NullPointerException e) {
			return;
		}

		Concept_Class target_cl;
		String target;

		target_cl = Utilities.getMapped_NPPrunedString(annotatedDoc, rel.getTargetId());

		if(target_cl.getName().equals("that") || target_cl.getName().equals("which")) {
			target_cl = Utilities.getRealTarget(annotatedDoc, VP, rel.getTargetId());
		}

		target = target_cl.getName();

		features.put("string", VP1 + " " + relation + " " + target);
		//features.put("string", VP1 + " " + relation + " " + inputAS.get(rel.getTargetId()).getFeatures().get("text"));
		features.put("source_ID", VP.getId());
		features.put("source_Type", "VP");
		features.put("source_String", VP1);

		features.put("target_ID", target_cl.getID());
		features.put("target_Type", "Parse_NP");
		features.put("target_String", target);
		//features.put("relation_Type", rel.getType());
		features.put("relation_Type", relation);
		features.put("kind", "VP_NP");
		features.put("cardinality", target_cl.getCardinality());

		features.put("target_category", annotatedDoc.getAnnotations().get(rel.getTargetId()).getFeatures().get("category"));
		/*
		if(!(VP1.equals("provide")))
		{
			Utilities.addAnnotation(annotatedDoc, VP, inputAS.get(target_cl.getID()), features, "Chain_1");
		}
		 */
		Utilities.addAnnotation(annotatedDoc, VP, inputAS.get(target_cl.getID()), features, "Chain_1");

	}

//	
//	public static void extractPrepRelations(Document annotatedDoc) throws InvalidOffsetException
//	{
//		AnnotationSet preps_with = annotatedDoc.getAnnotations().get("Dep-prep_with");
//		
//		for(Annotation prep_with: preps_with)
//		{
//			
//			FeatureMap prep_with_features = prep_with.getFeatures();
//			FeatureMap updatedFeatures = Factory.newFeatureMap();
//		
//			int rel_t1 = (int) prep_with_features.get("token1_Id");
//			int rel_t2 = (int) prep_with_features.get("token2_Id");
//			
//			int Phrase1_Id = Utilities.getMapped_NP(annotatedDoc, rel_t1);
//			if(Phrase1_Id == 0)
//			{
//				Phrase1_Id = Utilities.getMapped_VP(annotatedDoc, rel_t1);
//				updatedFeatures.put("P1_Id", Phrase1_Id);
//				updatedFeatures.put("P1_Type", "VP");
//			}
//			else
//			{
//				updatedFeatures.put("P1_Id", Phrase1_Id);
//				updatedFeatures.put("P1_Type", "NP");
//			}
//			
//			int Phrase2_Id = Utilities.getMapped_NP(annotatedDoc, rel_t2);
//			if(Phrase2_Id == 0)
//			{
//				Phrase2_Id = Utilities.getMapped_VP(annotatedDoc, rel_t2);
//				updatedFeatures.put("P2_Id", Phrase2_Id);
//				updatedFeatures.put("P2_Type", "VP");
//			}
//			else
//			{
//				updatedFeatures.put("P2_Id", Phrase2_Id);
//				updatedFeatures.put("P2_Type", "NP");
//			}						
//			annotatedDoc.getAnnotations().add(Utils.start(prep_with), Utils.end(prep_with), "PP_with", updatedFeatures);			
//		}
//		
//	}


	// 不懂
	private static String getVPString(Document doc, Annotation VP)
	{
		AnnotationSet overlappingVP_adv = Utils.getOverlappingAnnotations(doc.getAnnotations(), VP, "Dep-advmod");
		AnnotationSet overlappingVP_PP = Utils.getOverlappingAnnotations(doc.getAnnotations(), VP, "VP_PP");
		String VPStr = Utils.stringFor(doc, VP);
		if(overlappingVP_adv.size() == 1)
		{
			Annotation annot = Utils.getOnlyAnn(overlappingVP_adv);
			VPStr = annot.getFeatures().get("token2").toString().equals(VPStr)?annot.getFeatures().get("token1").toString() + " " + VPStr:VPStr + " " + annot.getFeatures().get("token2").toString();
			isAdvMod = true;
		}
		
		//FIx Mapping of combined ADVMOD AND PP
		if(overlappingVP_PP.size() == 1)
		{
			VPStr = Utils.stringFor(doc, overlappingVP_PP);
		}
		return VPStr;
	}

}
