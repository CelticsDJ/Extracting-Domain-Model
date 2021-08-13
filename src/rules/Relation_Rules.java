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

import static utils.Utilities.*;

public class Relation_Rules {
	
	static HashMap<Integer, String> VPsMap = new HashMap<Integer, String>();
	static boolean isAdvMod = false;

	public static void extractRelations() throws InvalidOffsetException
	{
		Document annotatedDoc = GlobalVariables.annotated_doc;
		AnnotationSet Relation_Verb = annotatedDoc.getAnnotations().get("Relation_Verb");
		List<Annotation> VPs = Utils.inDocumentOrder(Relation_Verb);
				
		for(Annotation VP: VPs)
		{
			if(VP.getFeatures().get("category").toString().equals("JJR")) {
				continue;
			}

			int num_Objects = 0;
			isAdvMod = false;
			String relVerb = Utils.cleanStringFor(annotatedDoc, VP);
			String VPStr = VP.getFeatures().get("string").toString();
			
			FeatureMap VP_features = VP.getFeatures();
			FeatureMap updatedFeatures = Factory.newFeatureMap();

			//@SuppressWarnings("unchecked")
			List<DependencyRelation> list_dependencies = (List<DependencyRelation>) VP_features.get("dependencies");
			
			for(DependencyRelation rel: list_dependencies)
			{
				boolean flag = false;

				if(VP_features.get("string").toString().contains("associate")) {
					Annotation nextNP = null;

					List<DependencyRelation> nmods = new ArrayList<>();
					for (DependencyRelation dep : list_dependencies) {
						if (dep.getType().equals("nmod")) {
							nmods.add(dep);
						}
						if (dep.getType().equals("dobj")) {
							nextNP = annotatedDoc.getAnnotations().get(Utilities.getMapped_NP(annotatedDoc, dep.getTargetId()));
						}
					}
					if (nmods.size() > 1 && nextNP != null) {
						List<DependencyRelation> updateDeps = (List<DependencyRelation>) nextNP.getFeatures().get("dependencies");
						for (int i = 0; i < nmods.size(); ++i) {
							if (getMapped_NP(annotatedDoc, nmods.get(i).getTargetId()) > nextNP.getId()) {
								updateDeps.add(nmods.get(i));
							}
						}
						nextNP.getFeatures().replace("dependencies", updateDeps);
						flag = true;
					}
				}

				if(rel.getType().equals("nsubj") || rel.getType().equals("xsubj") || rel.getType().equals("vocative"))
				{
					int Subject_Id = Utilities.getMapped_NP(annotatedDoc, rel.getTargetId());
					String subjectStr =  "";
					try {
						subjectStr = annotatedDoc.getAnnotations().get(Subject_Id).getFeatures().get("pruned_string").toString();
					}
					catch (NullPointerException e) {
						subjectStr = annotatedDoc.getAnnotations().get(Subject_Id).getFeatures().get("string").toString();
					}
					if(subjectStr.equals("that") || subjectStr.equals("which")) {
						Subject_Id = getRealSource(annotatedDoc, VP, rel.getTargetId());
					}
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
					String subjectStr =  "";
					try {
						subjectStr = annotatedDoc.getAnnotations().get(Subject_Id).getFeatures().get("pruned_string").toString();
					}
					catch (NullPointerException e) {
						subjectStr = annotatedDoc.getAnnotations().get(Subject_Id).getFeatures().get("string").toString();
					}
					if(subjectStr.equals("that") || subjectStr.equals("which")) {
						Subject_Id = getRealSource(annotatedDoc, VP, rel.getTargetId());
					}
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
				else if(rel.getType().equals("nmod") && !flag)
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

			if(VP.getFeatures().get("category").toString().equals("VBG")) {
				for(DependencyRelation rel : list_dependencies) {

					if(rel.getType().equals("cop")) {
						updatedFeatures.put("isAdvMod", 1);
					}

					if(rel.getType().equals("conj")) {
						Annotation tmp = annotatedDoc.getAnnotations().get(rel.getTargetId());

						FeatureMap tmpFeatures = Factory.newFeatureMap();
						try {
							tmpFeatures.put("Subject", updatedFeatures.get("Subject"));
							tmpFeatures.put("isAdvMod", updatedFeatures.get("isAdvMod"));
						}catch(NullPointerException e) {
							System.out.println("piu");
						}
						tmpFeatures.put("Num_Objects", updatedFeatures.get("Num_Objects"));
						tmpFeatures.put("str", tmp.getFeatures().get("string"));
						tmpFeatures.put("root", tmp.getFeatures().get("root"));

						annotatedDoc.getAnnotations().add(Utils.start(tmp), Utils.end(tmp), "Relations", tmpFeatures);
					}
				}
			}

			annotatedDoc.getAnnotations().add(Utils.start(VP), Utils.end(VP), "Relations", updatedFeatures);
		}
	}

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
