package processing;

import java.io.IOException;
import java.util.*;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;
import data.Association_Relation;
import data.Concept_Class;
import data.Concept_Relation;
import data.GlobalVariables;
import data.MetaRelation;
import data.RelationType;
import data.Requirement_Relations;
import gate.stanford.DependencyRelation;
import rules.Classes_Rules;
import utils.StringQuadruple;
import utils.StringTuple;
import utils.Utilities;

public class ExtractRelations_includingChains {
	
	private static int num_Objects = 0;
	private static int subject_Id = 0;
	private static int object_Id = 0;
	private static int subjectpass_Id = 0;
	private static int agent_Id = 0;
	/*
	private static int prep_with_Id = 0;
	private static int prep_for_Id = 0;
	private static int prep_in_Id = 0;
	private static int prep_to_Id = 0;
	private static int prep_from_Id = 0;
	private static int prepc_according_to = 0;
	*/
	private static String rel_str = "";
	private static String rel_root = "";
	private static FeatureMap rel_features;
	private static boolean isXcomp = false;
	private static boolean isAdvMod = false;


	private static Document doc;
	private static AnnotationSet inputAS;

	private static int relationsCount = 0;
	private static List<String> concepts = new ArrayList<String>();
	
	public static HashMap<String, HashSet<Concept_Relation>> hashmap_reqId_Relations = new HashMap<>();
	public static HashSet<Requirement_Relations> hashmap_requirmenets_Relations = new HashSet<>();
	
	
	private static Document original_annotated_doc;
	private static int reqId = 0;
	private static int relId = 0;
	
	
	public static void traverseRelations() throws SecurityException, IOException
	{
		//original_annotated_doc = GlobalVariables.annotated_doc;

		hashmap_requirmenets_Relations.clear();
		hashmap_reqId_Relations.clear();
		reqId = relId = 0;
		
		doc = GlobalVariables.annotated_doc;
		inputAS = doc.getAnnotations();
		AnnotationSet sentences = inputAS.get("Sentence");
		List<Annotation> Sentences = gate.Utils.inDocumentOrder(sentences); //All Requirements
		
		for(Annotation sentence:  Sentences)
		{				
			//Assign Requirement IDs 
			reqId++;
			relId = 1;

			if(reqId == 30) {
				reqId = 30;
			}

			AnnotationSet relations = gate.Utils.getContainedAnnotations(inputAS, sentence, "Relations");
			List<Annotation> Relations = gate.Utils.inDocumentOrder(relations);
			//List<MetaRelation> sentence_Relations = new ArrayList<MetaRelation>();
			for(Annotation relation:  Relations)//Traverse all the potential relations (verbs)
			{
				//Initialize all variables
				init(relation);
				
				MetaRelation meta_relation = new MetaRelation(relation.getId());
				List<StringQuadruple> relation_quads = Chaining.traverseVP_Chains(doc, relation, 0);
				meta_relation.setRelationChain(relation_quads);
				if(subject_Id != 0)
				{
					List<StringQuadruple> subject_quads = Chaining.getSubjectChains(doc, inputAS.get(subject_Id), 0);
					meta_relation.setSubjects(subject_quads);
				}
				
				if(num_Objects == 0)
				{
					traverseRelationwithZEROObject(relation, relation_quads);
				}
				else if(num_Objects ==1)
				{
					List<StringQuadruple> object_quads = Chaining.getObjectChains(doc, inputAS.get(object_Id), 0);
 					meta_relation.setObjects(object_quads);
				}
				else if(num_Objects > 1)
				{
					for(int i = 1; i<=num_Objects; i++)
					{							
						int obj_Id = rel_features.get("D_Object_" + i) == null? 0:  (int) rel_features.get("D_Object_" + i);
						List<StringQuadruple> object_quads = Chaining.getObjectChains(doc, inputAS.get(obj_Id), 0);
						meta_relation.setObjects(object_quads);
					}
				}
				createRelationCombinations(meta_relation);
				//sentence_Relations.add(meta_relation);
			}
			extractNP0fNP(doc, sentence);
			extractNPforNP(doc, sentence, "NP_for_NP");
			extractNPforNP(doc, sentence, "NP_from_NP");
			extractPossNP(doc, sentence);
			extractAdjectivallyModifiedNPs(doc, sentence);
			hashmap_requirmenets_Relations.add(new Requirement_Relations("R"+reqId, gate.Utils.stringFor(doc, sentence), hashmap_reqId_Relations.get("R"+reqId)));
		}
		markDuplicateRelations();
	}	
	
	private static void createRelationCombinations(MetaRelation rel) {

		if(rel.getSubjects().size() > 0 && rel.getRelationChains().size() > 1 && rel.getObjects().size() > 1) {
			//由于NLP工具导致的问题
			System.out.println("NLP problem: R" + reqId);
		}

		if(rel.getObjects().size() == 0 || rel.getRelationChains().size() == 0) {
			return;
		}

		boolean provide_pattern = false;

		if(rel.getRelationChains().get(0).getB().contains("provide")) {
			provide_pattern = true;
		}

		int obj_depth_bound = rel.getObjects().get(rel.getObjects().size()-1).getDepth();
		int rel_depth_bound = rel.getRelationChains().get(rel.getRelationChains().size()-1).getDepth();

		if(provide_pattern) {
			rel_depth_bound = 2;
		}

		obj_depth_bound = (obj_depth_bound + 1) / 2 - 1;
		rel_depth_bound = (rel_depth_bound + 1) / 2 - 1;

		if (rel.getObjects().size() == 1) {

			StringQuadruple obj_chain = rel.getObjects().get(0);

			String objStr = obj_chain.getC();
			String verbStr = rel.getRelationChains().get(0).getB();

			for (StringQuadruple rel_chain : rel.getRelationChains()) {
				for (StringQuadruple subj_chain : rel.getSubjects()) {

					//过滤depth较低的关联关系
					/*if((rel_chain.getDepth() < rel_depth_bound || obj_chain.getDepth() < obj_depth_bound) && !provide_pattern) {
						continue;
					}*/

					if (rel_chain.getDepth() == 0 || rel_chain.getDepth() == rel_depth_bound || rel_chain.getD().equals("JJ")) {
						Association_Relation association = Utilities.formRelations(subj_chain.getC(), objStr, subj_chain.getD(), obj_chain.getD(), (subj_chain.getB() + " " + rel_chain.getB() + " " + obj_chain.getB() + " " + rel_chain.getA() + " " + rel_chain.getC()).replace("  ", " ").replace("  ", " ").replace("  ", " ").trim(), isXcomp, "Association");
						addRelation(association);
					} else {
						Association_Relation association = Utilities.formRelations(subj_chain.getC(), rel_chain.getC(), subj_chain.getD(), obj_chain.getD(), (subj_chain.getB() + " " + rel_chain.getB() + " " + obj_chain.getB() + " " + rel_chain.getA()).replace(verbStr, verbStr + " " +  objStr).replace("  ", " ").replace("  ", " ").replace("  ", " ").trim(), isXcomp, "Association");
						addRelation(association);
					}
				}
			}

		}

		else {
			for (StringQuadruple rel_chain : rel.getRelationChains()) {
				for (StringQuadruple subj_chain : rel.getSubjects()) {
					for (StringQuadruple obj_chain : rel.getObjects()) {

						/*if((rel_chain.getDepth() < rel_depth_bound || obj_chain.getDepth() < obj_depth_bound) && !provide_pattern) {
							continue;
						}*/

						Association_Relation association = Utilities.formRelations(subj_chain.getC(), obj_chain.getC(), subj_chain.getD(), obj_chain.getD(), (subj_chain.getB() + " " + rel_chain.getB() + " " + obj_chain.getB() + " " + rel_chain.getA() + " " + rel_chain.getC()).replace("  ", " ").replace("  ", " ").replace("  ", " ").trim(), isXcomp, "Association");
						addRelation(association);
					}
				}
			}
		}
	}
	
	private static void traverseRelationwithZEROObject(Annotation relation, List<StringQuadruple> quads)
	{
		for(StringQuadruple quad: quads)
		{
			String verb = quad.getA();
			String iobj = quad.getC();
			
			if(subject_Id != 0)
			{						
				List<StringQuadruple> subject_quads = Chaining.getSubjectChains(doc, inputAS.get(subject_Id), 0);
				for(StringQuadruple subject_quad: subject_quads)
				{
					if(!iobj.equals(""))
					{
						Association_Relation rel = Utilities.formRelations(subject_quad.getC(), iobj, subject_quad.getD(), quad.getD(), subject_quad.getB() + " " + rel_root + " " + verb, isXcomp, "Association");
						addRelation(rel);						
						relationsCount++;
					}
					//else if(quads.size() <= 1) //Only add attribute when there is no other choice, i.e., there is no other chain to the verb
					if(isAdvMod)
					{
						Concept_Relation rel = new Concept_Relation(new Concept_Class(subject_quad.getC()), new Concept_Class(rel_str), RelationType.ATTRIBUTE, "Attribuate");
						addRelation(rel);
					}
				}
			}
			else if(subjectpass_Id != 0)
			{
				List<StringQuadruple> subject_quads = Chaining.getSubjectChains(doc, inputAS.get(subjectpass_Id), 0);
				for(StringQuadruple subject_quad: subject_quads){
					String subj = subject_quad.getC();
					if(agent_Id != 0)
					 {
						 Concept_Class I_Obj = Utilities.getMapped_NPPrunedString(doc, agent_Id);
						 iobj = I_Obj.getName();
								 
						 Association_Relation rel = Utilities.formRelations(iobj, subj, I_Obj.getCardinality(), subject_quad.getD(), rel_root + " " + quad.getA() + " " + quad.getB() +  " " + quad.getC(), isXcomp, "B1");
						 addRelation(rel);
						 relationsCount++;
					 }		
					else if(!iobj.equals(""))
					{
						String relationText = (quad.getB().isEmpty()?rel_root  + " " + verb:quad.getB().replace(rel_str, rel_root   + " " + verb));
						//String relationText = (quad.getB().isEmpty()?rel_root  + " " + verb:quad.getB().replace(rel, rel_root + verb));
						Association_Relation rel = Utilities.formRelations(subj, iobj, subject_quad.getD(), quad.getD(), relationText, isXcomp, "B1");
						addRelation(rel);
						relationsCount++;
					}
					//else if(quads.size() <= 1) //Only add attribute when there is no other choice, i.e., there is no other chain to the verb
					if(isAdvMod)
					{
						Concept_Relation rel = new Concept_Relation(new Concept_Class(subj), new Concept_Class(rel_str), RelationType.ATTRIBUTE, "Attribuate");
						addRelation(rel);
					}
				}				
			}
		}
	}
	
	/*
	 * Output List of triples
	 * 1 Quadruple = (is altered, relation chain, object, cardinality)
	 */
	private static List<StringQuadruple> traverseNP_Chains(Annotation Source_Annot, int depth)
	{		
		Concept_Class object = Utilities.getMapped_NPPrunedString(doc, Source_Annot.getId()); //Get class of the source annotation
		String source = object.getName(); //Source string 
		concepts.add(source.toLowerCase());
		String cardinality = object.getCardinality(); //Source cardinality (A decision to have the cardinality of the chain as the cardinality of the source)
		String altered = "false"; //This is a bool to tell you that chain is yet unaltered 
		
		List<Annotation> list_chains = Utilities.isChainSource(doc, Source_Annot.getId()); //List of all the chains with Source_Annot as source
		String chain = "";
		String final_concept = "";
		List<StringQuadruple> return_list = new ArrayList<StringQuadruple>();  //Return List
		
		/*
		 * Return blank chain string if there is no chain from this source
		 */
		if(list_chains.size() == 0)
		{
			//return_list.add(new StringQuadruple("false", "", "", "1"));
			return return_list;
		}		
		/*
		 * If there are more than 1 chains that begin from this source.
		 * Example: "confirmation from the user for this action and subsequent related actions in project"
		 * 
		 * Output: "confirmation from user"
		 * Output: "confirmation from user for action"
		 * Output: "confirmation from user for subsequent related actions"
		 * Output: "confirmation from user for subsequent related actions in project"  (This is from recursion in previous IF)
		 */
		else
		{
			String prev_PP = "";			
			String base_old = "";
			String base_new = "";
			
			return_list.add(new StringQuadruple(altered, "", source, cardinality, depth));
			
			for(Annotation chain_NP: list_chains)
			{
				FeatureMap chainFeatures = chain_NP.getFeatures();
				if(chainFeatures.get("kind").toString().equals("NP_NP") && chainFeatures.get("string").toString().contains(source))
				{
					String PP = chainFeatures.get("relation_Type").toString().replaceAll("prep(c)?", "").replaceAll("_", " ");
					chain = source + " " + PP + " " + chainFeatures.get("target_String").toString();
					final_concept = chainFeatures.get("target_String").toString();
					
					
					concepts.add(chainFeatures.get("target_String").toString().toLowerCase());
					cardinality = chainFeatures.get("cardinality").toString();
					altered = "true";
					
					if(prev_PP.equals("")) //First chain
					{
						StringTuple tuple = Utilities.getRelation_Target(chain + "###" + final_concept);
						return_list.add(new StringQuadruple(altered, tuple.getA(), tuple.getB(), cardinality, depth));
						base_old = source;
						base_new = chain;
					}					
					else if(prev_PP.equals(PP)) //Chain with same prepositions, e.g., "confirmation from the user FOR (this action) and (subsequent related actions)"
					{												
						StringTuple tuple = Utilities.getRelation_Target(chain + "###" + final_concept);
						return_list.add(new StringQuadruple(altered, base_old + " " + PP, final_concept, cardinality, depth));
						base_new = base_old + " " + PP + " " + chainFeatures.get("target_String").toString();
					}
					else //Chain with different continuous prepositions, e.g., "confirmation FROM the user FOR this action"
					{
						base_old = base_new;
						base_new = base_new + " " + PP + " " + chainFeatures.get("target_String").toString();
						final_concept = chainFeatures.get("target_String").toString();
						return_list.add(new StringQuadruple(altered, base_new.replace(final_concept, ""), final_concept, cardinality, depth));
					}
					prev_PP = PP;
					
					for(StringQuadruple quad: traverseNP_Chains(inputAS.get(Integer.parseInt(chainFeatures.get("target_ID").toString())), depth+1)) //RECURSION
					{
						if(quad.getA() == "true")
							{								
								return_list.add(new StringQuadruple(altered, source + " " + PP + " " + quad.getB(), quad.getC(), quad.getD(), quad.getDepth()));
							}
					}				
				}		
			}
		}
		return return_list;
	}
	

		
	//Initialize all variables
	private static void init(Annotation relation)
	{
		rel_features = relation.getFeatures();		
		num_Objects = (int) rel_features.get("Num_Objects");
		subject_Id = rel_features.get("Subject") == null? 0:  (int) rel_features.get("Subject");
		object_Id = rel_features.get("D_Object_1") == null? 0:  (int) rel_features.get("D_Object_1");
		subjectpass_Id = rel_features.get("Passive_Subject") == null? 0:  (int) rel_features.get("Passive_Subject");
		agent_Id = rel_features.get("Agent") == null? 0:  (int) rel_features.get("Agent");
		/*
		prep_with_Id = rel_features.get("prep_with") == null? 0:  (int) rel_features.get("prep_with");
		prep_for_Id = rel_features.get("prep_for") == null? 0:  (int) rel_features.get("prep_for");
		prep_in_Id = rel_features.get("prep_in") == null? 0:  (int) rel_features.get("prep_in");
		prep_to_Id = rel_features.get("prep_to") == null? 0:  (int) rel_features.get("prep_to");
		prep_from_Id = rel_features.get("prep_from") == null? 0:  (int) rel_features.get("prep_from");
		*/
		rel_str = rel_features.get("str").toString();
		rel_root = rel_features.get("root").toString();
		/*
		prepc_according_to = ((rel_features.get("prepc_according_to") == null) || (rel_features.get("P_Object") == null))? 0:  (int) rel_features.get("P_Object");
		 */
		isXcomp = rel_features.get("xcomp") == null? false: true;
		isAdvMod = rel_features.get("isAdvMod") == null? false: true;
	}

	private static void explore_VmodDependencies_overlappingwithProvide(Annotation provide_Pattern)
	{	
		AnnotationSet overlapping_vmod = Utils.getOverlappingAnnotations(inputAS, provide_Pattern, "Dep-vmod");
		AnnotationSet contained_vmod = Utils.getContainedAnnotations(inputAS, provide_Pattern, "Dep-vmod");
		AnnotationSet annots = Utils.minus(overlapping_vmod, contained_vmod);
		
		for(Annotation annot: annots)
		{
			int token_Id = Integer.parseInt(annot.getFeatures().get("token2_Id").toString());
			Annotation token = inputAS.get(token_Id);
			AnnotationSet VPs = Utils.getCoextensiveAnnotations(inputAS, token, "Relations");
			if(VPs.size() == 1)
			{
				Annotation VP = Utils.getOnlyAnn(VPs);
				VP.getFeatures().put("Subject", subject_Id);
			}
		}
	}
	
	public static void extractNPforNP(Document annotated_Doc, Annotation sentence, String annot_name)
	{
		AnnotationSet relations = gate.Utils.getContainedAnnotations(inputAS, sentence, annot_name);
		List<Annotation> Relations = gate.Utils.inDocumentOrder(relations);
		for(Annotation relation:  Relations)
		{
			AnnotationSet NPs = gate.Utils.getContainedAnnotations(inputAS, relation, "Parse_NP");
			List<Annotation> list_NPs = gate.Utils.inDocumentOrder(NPs);
			if(list_NPs.size() == 2)
			{
				String NP1 = Utilities.getMapped_NPPrunedString(doc, list_NPs.get(0).getId()).getName();
				String NP2 = Utilities.getMapped_NPPrunedString(doc, list_NPs.get(1).getId()).getName();
				relationsCount++;
			}
		}
	}
	
	public static void extractAdjectivallyModifiedNPs(Document annotated_Doc, Annotation sentence)
	{
		/*
		AnnotationSet NPs = gate.Utils.getContainedAnnotations(inputAS, sentence, "Parse_NP");
		List<Annotation> list_NPs = gate.Utils.inDocumentOrder(NPs);
		for(Annotation NP:  list_NPs)
		{
			HashMap<String, Set<String>> map_AdjNPs = Classes_Rules.manageModifiers(NP,NP.getFeatures());
			for(String key: map_AdjNPs.keySet())
			{
				for(String obj: map_AdjNPs.get(key))
				{
					Concept_Relation rel = new Concept_Relation(new Concept_Class(obj), new Concept_Class(key), RelationType.GENERALIZATION, "Generalization");
					addRelation(rel);
				}
			}
		}
		*/

		AnnotationSet tokenSet = gate.Utils.getContainedAnnotations(inputAS, sentence,"Token");
		List<Annotation> tokens = gate.Utils.inDocumentOrder(tokenSet);
		for(Annotation token : tokens) {
			List<DependencyRelation> dependencies = (List<DependencyRelation>) token.getFeatures().get("dependencies");
			try {
				for (DependencyRelation dep : dependencies) {
					if (dep.getType().equals("compound")) {
						String base_string = token.getFeatures().get("string").toString();

						int compound_target = dep.getTargetId();
						String compound_string = "";
						while (compound_target <= token.getId()) {
							compound_string =  compound_string.concat(tokenSet.get(compound_target).getFeatures().get("string").toString() + " ");
							compound_target += 2;
						}

						compound_target = dep.getTargetId();
						int base_id = token.getId();
						while(base_id > compound_target + 2) {
							base_id -= 2;
							base_string = tokenSet.get(base_id).getFeatures().get("string").toString() + " " + base_string;
						}

						Concept_Relation rel = new Concept_Relation(new Concept_Class(compound_string.trim()), new Concept_Class(base_string), RelationType.GENERALIZATION, "Generalization");
						addRelation(rel);
					}
				}
			}catch (Exception e) {

			}
		}
	}
	
	
		
	
	public static void extractNP0fNP(Document annotated_Doc, Annotation sentence)
	{
		AnnotationSet relations = gate.Utils.getContainedAnnotations(inputAS, sentence, "NP_of_NP");
		List<Annotation> Relations = gate.Utils.inDocumentOrder(relations);
		for(Annotation relation:  Relations)
		{
			FeatureMap rel_features = relation.getFeatures();
			int NP1_id = rel_features.get("NP1_id") == null? 0:  (int) rel_features.get("NP1_id");
			int NP2_id = rel_features.get("NP2_id") == null? 0:  (int) rel_features.get("NP2_id");
			Annotation NP1annot = annotated_Doc.getAnnotations().get(NP1_id);
			Annotation NP2annot = annotated_Doc.getAnnotations().get(NP2_id);
			String NP1 = Utilities.getMapped_NPPrunedString(doc, NP1annot.getId()).getName();
			String NP2 = Utilities.getMapped_NPPrunedString(doc, NP2annot.getId()).getName();
			Concept_Relation rel = new Concept_Relation(new Concept_Class(NP1), new Concept_Class(NP2), RelationType.AGGREGATION, "Aggregation");

			addRelation(rel);
			relationsCount++;
			
			FeatureMap NP_features = NP1annot.getFeatures();
			if(NP_features.get("pruned_structure").toString().contains("JJ") && NP_features.get("validNN").toString().equals("true"))
			{
				String NP_string = NP_features.get("pruned_string").toString();
				int startPos_BaseNP = Classes_Rules.returnBaseofAdjectivialNP(NP_features.get("pruned_structure").toString(), NP_string);
				String base_NP = NP_string.substring(startPos_BaseNP, NP_string.length()).toLowerCase();
//				if(Classes_Rules.map_adjNPs.containsKey(base_NP))
//				{
//					Classes_Rules.map_adjNPs.get(base_NP).add(NP1 + " of " + NP2);
//				}
//				else
//				{
//					Classes_Rules.map_adjNPs.put(base_NP, new HashSet<String>());
//					Classes_Rules.map_adjNPs.get(base_NP).add(NP1 + " of " + NP2);
//				}
			}
		}
	}
	
	public static void extractPossNP(Document annotated_Doc,  Annotation sentence){
		AnnotationSet relations = gate.Utils.getContainedAnnotations(inputAS, sentence, "Dep-nmod:poss");
		List<Annotation> Relations = gate.Utils.inDocumentOrder(relations);
		
		for(Annotation relation:  Relations)
		{
			/*
			FeatureMap rel_features = relation.getFeatures();
			String str = rel_features.get("pruned_string").toString();
			String [] NPs = str.split("\'s");
			if(NPs.length == 2)
			{
				System.out.println(System.lineSeparator() +"NP's NP:  " +  NPs[0] + " --> has --> " + NPs[1]);
				Concept_Relation rel = new Concept_Relation(new Concept_Class(NPs[0]), new Concept_Class(NPs[1]), RelationType.AGGREGATION, "Aggregation");

				addRelation(rel);
				relationsCount++;
			}*/
			//Concept_Relation rel = new Concept_Relation(new Concept_Class(relation.getFeatures().get("token1").toString()), new Concept_Class(relation.getFeatures().get("token2").toString()), RelationType.AGGREGATION, "Aggregation");
			Concept_Relation rel = new Concept_Relation(new Concept_Class(relation.getFeatures().get("token1").toString()), new Concept_Class(relation.getFeatures().get("token2").toString()), RelationType.ATTRIBUTE, "Attribuate");

			addRelation(rel);
			relationsCount++;
		}
		
	}
	
	public static void addRelation(Object relObj)
	{
		try {
			Association_Relation tmp = (Association_Relation) relObj;
			if (tmp.getRelationName().equals("provide") || tmp.getRelationName().equals("provide user with") || tmp.getRelationName().equals("provide with ability")) {
				return;
			}

			if(tmp.getRelationName().contains("provide") && !tmp.getRelationName().contains("provide user with")) {
				return;
			}
			if (tmp.getTarget().getName().endsWith("ed")) {
				return;
			}
			if(tmp.getRelationName().startsWith("include") && tmp.getRelationName().endsWith("in")) {
				Concept_Relation include_pattern = new Concept_Relation(new Concept_Class(tmp.getRelationName().replace("include", "").replace("in", "").trim()), new Concept_Class(tmp.getTarget().getName()), RelationType.AGGREGATION, "D6");
				addRelation(include_pattern);
				return;
			}
		}
		catch(Exception e) {

		}

		Concept_Relation rel = (Concept_Relation) relObj;
		HashSet<Concept_Relation> relations = hashmap_reqId_Relations.get("R"+reqId);
		if(relations == null)
		{		
			relations = new HashSet<Concept_Relation>();		
		}

		if(rel.getSource().getName().trim().isEmpty() || rel.getTarget().getName().trim().isEmpty())
		{
			return;
		}
		rel.setSequence_Num(relId++);
		relations.add(rel);
		hashmap_reqId_Relations.put("R"+reqId, relations);
	}

	public static void markDuplicateRelations() {

		List<Concept_Relation> concept_relations = new ArrayList<>();
		List<Association_Relation> association_relations = new ArrayList<>();

		Integer req_id = 1;
		while(req_id <= ExtractRelations_includingChains.hashmap_requirmenets_Relations.size()) {
			for (Requirement_Relations req_relations : hashmap_requirmenets_Relations) {
				if (!req_relations.Req_Id.equals("R" + req_id.toString())) {
					continue;
				}
				req_id++;

				if (req_relations.relations == null) {
					continue;
				}
				Iterator it = req_relations.relations.iterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (obj.getClass().toString().contains("Concept_Relation")) {
						Concept_Relation rel = (Concept_Relation) obj;
						if (!rel.getRelationType().equals(RelationType.ASSOCIATION)) {
							for (Concept_Relation tmp : concept_relations) {
								if (rel.getSource().getName().equals(tmp.getSource().getName()) && rel.getTarget().getName().equals(tmp.getTarget().getName()) && rel.getRelationType().equals(tmp.getRelationType())) {
									rel.setDuplicateStatus(true);
								}
							}

							if (!rel.getDuplicateStatus()) {
								concept_relations.add(rel);
							}
						}
					}
					else {
						//Association去重
						Association_Relation rel = (Association_Relation) obj;
						for(Association_Relation tmp : association_relations) {
							if(rel.getSource().getName().equals(tmp.getSource().getName()) && rel.getTarget().getName().equals(tmp.getTarget().getName()) && rel.getRelationName().equals(tmp.getRelationName())) {
								rel.setDuplicateStatus(true);
							}
						}
						if(!rel.getDuplicateStatus()) {
							association_relations.add(rel);
						}
					}
				}
			}
		}
		return;
	}

}


