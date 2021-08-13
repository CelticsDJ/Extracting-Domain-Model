package rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import gate.*;
import gate.util.InvalidOffsetException;
import gate.stanford.DependencyRelation;
import data.Concept_Class;
import data.GlobalVariables;
import utils.Utilities;

import static utils.Utilities.*;

public class Classes_Rules {
	
	public static HashSet<String> set_Concepts = new HashSet<String>();
	private static Document annotatedDoc;
	private static AnnotationSet inputAS;
	
	public static void classesInfo() throws InvalidOffsetException
	{
		//Declarations
		Classes_Rules.annotatedDoc = GlobalVariables.annotated_doc;
		inputAS = annotatedDoc.getAnnotations();

		AnnotationSet NPset = inputAS.get("Parse_NP");
		List<Annotation> NPs = Utils.inDocumentOrder(NPset);
		AnnotationSet tmp = annotatedDoc.getAnnotations();
		String [] acceptable_dependencies = {"rcmod", "prep", "nmod", "acl", "advcl", "dep"};
		
		/*
		 * Traverse through all the Parse_NPs 
		 */		
		for(Annotation NP: NPs)
		{
			FeatureMap NP_features = NP.getFeatures();
			FeatureMap updatedFeatures = Factory.newFeatureMap();
			
			//Maintain a set of all concepts
			set_Concepts.add((String) NP_features.get("pruned_string").toString());
			
			//@SuppressWarnings("unchecked")
			List<DependencyRelation> list_dependencies = (NP_features.get("dependencies") == null)?new ArrayList<>():(List<DependencyRelation>) NP_features.get("dependencies");
						
			if(list_dependencies.size() > 0)
				{
					List<DependencyRelation> nmods = new ArrayList<>();
					for(DependencyRelation dep : list_dependencies) {
						if(dep.getType().equals("nmod")) {
							nmods.add(dep);
						}
					}
					if(nmods.size() > 1) {
						Annotation nextNP = annotatedDoc.getAnnotations().get(Utilities.getMapped_NP(annotatedDoc, nmods.get(0).getTargetId()));
						List<DependencyRelation> updateDeps = (List<DependencyRelation>) nextNP.getFeatures().get("dependencies");
						for(int i = 1; i < nmods.size(); ++i) {
							if(getMapped_NP(annotatedDoc, nmods.get(i).getTargetId()) > nextNP.getId()) {
								updateDeps.add(nmods.get(i));
							}
						}
						nextNP.getFeatures().replace("dependencies", updateDeps);
					}

					for(DependencyRelation rel: list_dependencies)
					{
						if(Arrays.stream(acceptable_dependencies).parallel().anyMatch(rel.getType()::contains)) //Java 8 stream API
						{
							if(rel.getType().equals("dep") && NP_features.get("pruned_string").equals("ability")) {
								rel.setType("acl");
							}

							//暂时不处理nmod:poss
							if(rel.getType().equals("nmod:poss") || rel.getType().equals("acl:relcl")) {
								continue;
							}

							updatedFeatures.put("FD_" + rel.getType(), Utilities.getMapped_NP(annotatedDoc, rel.getTargetId())); //Mark forward dependencies
							buildChains(rel, NP);
						}						
					}
				}
			/*
			 * Add a new Annotation - Classes
			 */
			annotatedDoc.getAnnotations().add(Utils.start(NP), Utils.end(NP), "Classes", updatedFeatures);					
		}
	}
	
	public static int returnBaseofAdjectivialNP(String NP_structure, String NP_string)
	{
		String [] arr_POS = NP_structure.split("-");
		String [] arr_str = NP_string.split(" ");
		int len = 0;
		for(int i = 0; i< arr_POS.length; i++)
		{			
			if(arr_POS[i].contains("NN"))
			{
				return len;
			}
			len += arr_str[i].length() + 1;
		}
		return 0;
	}
	
	/*
	 *  Build Chain from the relation
	 */
	private static void buildChains(DependencyRelation rel, Annotation NP)
	{
		FeatureMap features = Factory.newFeatureMap();
		Concept_Class NP1 = Utilities.getMapped_NPPrunedString(annotatedDoc, NP.getId());

		Concept_Class target_cl;
		String target;

		Integer target_id = rel.getTargetId();
		String relation = "";

		if(rel.getType().equals("acl") || rel.getType().equals("advcl")) {

			Annotation VB = annotatedDoc.getAnnotations().get(target_id);

			relation = relation.concat(VB.getFeatures().get("string").toString());

			List<DependencyRelation> dependencies = (List<DependencyRelation>)VB.getFeatures().get("dependencies");

			for(DependencyRelation dep : dependencies) {

				if(dep.getType().equals("dobj")) {
					target_id = dep.getTargetId();
				}

				if(dep.getType().equals("case") || dep.getType().equals("mark")) {
					relation = annotatedDoc.getAnnotations().get(dep.getTargetId()).getFeatures().get("string").toString().concat(" ").concat(relation);
				}

				if(dep.getType().equals("nmod")) {

					boolean flag = false;
					for(DependencyRelation dep2 : dependencies) {
						if(dep2.getType().equals("dobj")) {
							target_id = dep2.getTargetId();
							flag = true;
							break;
						}
					}

					if(flag && getMapped_NP(annotatedDoc, dep.getTargetId()) != getMapped_NP(annotatedDoc, target_id)) {
						try {
							List<DependencyRelation> updateDeps = (List<DependencyRelation>) annotatedDoc.getAnnotations().get(getMapped_NP(annotatedDoc, target_id)).getFeatures().get("dependencies");
							updateDeps.add(dep);
							annotatedDoc.getAnnotations().get(target_id).getFeatures().replace("dependencies", updateDeps);
						}catch (Exception e) {
							System.out.println(e);
						}
					}
				}
			}
		}

		else {
			try {
				relation = getRelationType(annotatedDoc, rel).replace("according", "according to");
			}catch (NullPointerException e) {
				return;
			}
		}

		target_cl = Utilities.getMapped_NPPrunedString(annotatedDoc, target_id);
		target = target_cl.getName();




		if(NP1.getID() == target_cl.getID()) {
			return;
		}


		features.put("source_ID", NP1.getID());
		features.put("source_Type", "Parse_NP");
		features.put("source_String", NP1.getName());

		features.put("target_ID", target_cl.getID());
		features.put("target_Type", "Parse_NP");
		features.put("target_String", target);
		//features.put("relation_Type", rel.getType());
		features.put("relation_Type", relation);
		features.put("kind", "NP_NP");

		features.put("cardinality", NP1.getCardinality());

		if(NP1.getName().equals(target)) {
			System.out.println(target + " " + rel.getType() + " " + target);
		}

		Utilities.addAnnotation(annotatedDoc, NP, inputAS.get(target_cl.getID()), features, "Chain_1");

		if(relation.equals("of") || relation.equals("from") || relation.contains("include")) {
			if(relation.contains("include")) {
				Annotation VB = annotatedDoc.getAnnotations().get(target_id);
				List<DependencyRelation> dependencies = (List<DependencyRelation>) VB.getFeatures().get("dependencies");
				for(DependencyRelation dep : dependencies) {
					if(dep.getType().equals("nmod")) {
						target_id = dep.getTargetId();
					}
				}
				relation = "of";
			}

			Annotation NP_1 = annotatedDoc.getAnnotations().get(NP.getId());
			Annotation NP_2 = annotatedDoc.getAnnotations().get(target_id);

			Node start = NP_1.getStartNode();
			Node end = NP_2.getEndNode();

			FeatureMap featureMap = Factory.newFeatureMap();
			featureMap.put("NP1_id", NP_1.getId());
			featureMap.put("NP2_id", NP_2.getId());

			String annotation_type = "NP_" + relation + "_NP";

			annotatedDoc.getAnnotations().add(start, end, annotation_type, featureMap);
		}
											
	}
}
