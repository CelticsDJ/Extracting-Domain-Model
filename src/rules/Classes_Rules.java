package rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class Classes_Rules {
	
	public static HashSet<String> set_Concepts = new HashSet<String>();
	private static Document annotatedDoc;
	private static AnnotationSet inputAS;
	
	/*
	 * Manage all the information related to classes
	 * Tasks - 
	 * 1) Build NP chain
	 * 2) Manage Adjectively Modified NPs
	 * 3) Manage special annotation, such as rcmod
	 */
	
	public static void classesInfo() throws InvalidOffsetException
	{
		//Declarations
		Classes_Rules.annotatedDoc = GlobalVariables.annotated_doc;
		inputAS = annotatedDoc.getAnnotations();

		AnnotationSet NPset = inputAS.get("Parse_NP");
		List<Annotation> NPs = Utils.inDocumentOrder(NPset);
		AnnotationSet tmp = annotatedDoc.getAnnotations();
		/*ArrayList<Annotation> NPs = new ArrayList<>();
		for (Annotation a : tmp) {
			if (a.getType().equals("Token")) {
				if(a.getFeatures().get("category").equals("NN") && a.getFeatures().get("dependencies") != null){
					NPs.add(a);
				}
			}
		}*/
		String [] acceptable_dependencies = {"rcmod", "prep", "nmod"};
		
		/*
		 * Traverse through all the Parse_NPs 
		 */		
		for(Annotation NP: NPs)
		{
			FeatureMap NP_features = NP.getFeatures();
			FeatureMap updatedFeatures = Factory.newFeatureMap();
			
			//Maintain a set of all concepts
			set_Concepts.add((String) NP_features.get("pruned_string").toString());

			/*
			List<String> dependencies = (List<String>) NP_features.get("dependencies");
			List<DependencyRelation> list_dependencies = new ArrayList<>();
			for (String dep : dependencies) {
				list_dependencies.add(new DependencyRelation(dep));
			}
			*/
			
			//@SuppressWarnings("unchecked")
			List<DependencyRelation> list_dependencies = (NP_features.get("dependencies") == null)?new ArrayList<>():(List<DependencyRelation>) NP_features.get("dependencies");
						
			if(list_dependencies.size() > 0)
				{
					for(DependencyRelation rel: list_dependencies)
					{
						if(Arrays.stream(acceptable_dependencies).parallel().anyMatch(rel.getType()::contains)) //Java 8 stream API
						{

							//暂时不处理nmod:poss
							if(rel.getType().equals("nmod:poss")) {
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
	
	public static HashMap<String, Set<String>> manageAdjModifiers(Annotation NP, FeatureMap NP_features)
	{
		HashMap<String, Set<String>> map_adjNPs = new HashMap<String, Set<String>>();
		String NP_structure = (String) NP_features.get("pruned_structure").toString();
		String NP_string = (String) NP_features.get("pruned_string").toString();
		
		if(NP_structure.contains("JJ") && NP_features.get("validNN").toString().equals("true"))
		{
			int startPos_BaseNP = returnBaseofAdjectivialNP(NP_structure, NP_string);
			String base_NP = NP_string.substring(startPos_BaseNP, NP_string.length()).toLowerCase();
			if(map_adjNPs.containsKey(base_NP))
			{
				map_adjNPs.get(base_NP).add(Utilities.getMapped_NPPrunedString(annotatedDoc, NP.getId()).getName());
			}
			else
			{
				map_adjNPs.put(base_NP, new HashSet<String>());
				map_adjNPs.get(base_NP).add(Utilities.getMapped_NPPrunedString(annotatedDoc, NP.getId()).getName());
			}
		}
		return map_adjNPs;
	}
	
//	public static void printAdjNPs(Document doc)
//	{
//		for(String key: map_adjNPs.keySet())
//		{
//			System.out.println(System.lineSeparator() + key + " : ");
//			Set<String> lst_NPs = map_adjNPs.get(key);
//			for(String NP: lst_NPs)
//			{
//				System.out.println(NP);
//			}
//		}
//	}
	
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
		/*if(rel.getType().contains("prep"))
		{
				FeatureMap features = Factory.newFeatureMap();
				Concept_Class NP1 = Utilities.getMapped_NPPrunedString(annotatedDoc, NP.getId());					

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

				if(NP1.getID() == target_cl.getID()) {
					return;
				}


				features.put("source_ID", NP1.getID());
				features.put("source_Type", "Parse_NP");
				features.put("source_String", NP1.getName());

				features.put("target_ID", target_cl.getID());
				features.put("target_Type", "Parse_NP");
				features.put("target_String", target);
				features.put("relation_Type", rel.getType());
				features.put("kind", "NP_NP");
				
				features.put("cardinality", NP1.getCardinality());

				if(NP1.getName().equals(target)) {
					System.out.println(target + " " + rel.getType() + " " + target);
				}

				Utilities.addAnnotation(annotatedDoc, NP, inputAS.get(target_cl.getID()), features, "Chain_1");
		}*/
		FeatureMap features = Factory.newFeatureMap();
		Concept_Class NP1 = Utilities.getMapped_NPPrunedString(annotatedDoc, NP.getId());

		Concept_Class target_cl;
		String target;

		target_cl = Utilities.getMapped_NPPrunedString(annotatedDoc, rel.getTargetId());
		target = target_cl.getName();

		String relation = getRelationType(annotatedDoc, rel);


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
											
	}
}
