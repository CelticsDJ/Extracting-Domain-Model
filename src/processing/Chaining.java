package processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;
import data.Association_Relation;
import data.Concept_Class;
import utils.StringQuadruple;
import utils.StringTuple;
import utils.Utilities;
import utils.Utils_DuplicateCheck;

public class Chaining {
	
	/*
	 * Format of output - String Quadruple
	 * (isFirst, Chain, Subject, Cardinality)
	 * example Simulator for Satellite 
	 * (false, "", Simulator, 1)
	 * (true, "for Satellite", Simulator, 1)
	 */
	public static List<StringQuadruple> getSubjectChains(Document doc, Annotation subjAnnot, int depth)
	{
		Concept_Class subjectClass = Utilities.getMapped_NPPrunedString(doc, subjAnnot.getId()); //Get class of the source annotation
		String subj_Str = subjectClass.getName().trim();
		String subj_Cardinality = subjectClass.getCardinality().trim();
		String altered = "false";
		
		List<Annotation> list_chains = Utilities.isChainSource(doc, subjAnnot.getId()); //List of all the chains with Source_Annot as source
		String chain = "";
		List<StringQuadruple> return_list = new ArrayList<StringQuadruple>();  //Return List
		return_list.add(new StringQuadruple("false", "", subj_Str, subj_Cardinality, depth++));
		
		/*
		 * Processing
		 */
		
		/*
		 * Return blank chain string if there is no chain from this source
		 */
		if(list_chains.size() == 0)
		{
			return return_list;
		}		
		/*
		 * If there are more than 1 chains that begin from this source.
		 * Example: "confirmation from the user for this action and subsequent related actions in project"
		 * 
		 * Output: "Simulator for Satellite"
		 * Output: "Simulator for Satellite in Space"
		 */
		else
		{
			String prev_PP = "";
			String base_old = "";
			String base_new = "";
			
			for(Annotation chain_NP: list_chains)
			{
				FeatureMap chainFeatures = chain_NP.getFeatures();
				if(chainFeatures.get("kind").toString().equals("NP_NP") && chainFeatures.get("string").toString().contains(subj_Str))
				{
					String PP = chainFeatures.get("relation_Type").toString().replaceAll("prep(c)?_", "").replaceAll("_", " ");
					chain = PP + " " + chainFeatures.get("target_String").toString();
					altered = "true";
					
					if(prev_PP.equals("")) //First chain
					{
						return_list.add(new StringQuadruple(altered, chain.trim(), subj_Str, subj_Cardinality, depth++));
						base_old = "";
						base_new = chain;
					}
					else if(prev_PP.equals(PP))
					{
						chain = PP + " " + chainFeatures.get("target_String").toString();
						return_list.add(new StringQuadruple(altered, (base_old + " " + chain).trim(), subj_Str, subj_Cardinality, depth++));
						base_new = base_old + " " + chain;
					}
					else //Chain with different continuous prepositions, e.g., "confirmation FROM the user FOR this action"
					{
						chain = PP + " " + chainFeatures.get("target_String").toString();
						return_list.add(new StringQuadruple(altered, (base_new + " " + chain).trim(), subj_Str, subj_Cardinality, depth++));
						base_old = base_new;
						base_new = base_new + " " + chain;
					}
					
					prev_PP = PP;

					if(subjAnnot.getId().equals(doc.getAnnotations().get(Integer.parseInt(chainFeatures.get("target_ID").toString())).getId())) {
						return return_list;
					}
					
					for(StringQuadruple quad: getSubjectChains(doc, doc.getAnnotations().get(Integer.parseInt(chainFeatures.get("target_ID").toString())), depth+1)) //RECURSION
					{
						if(quad.getA() == "true")
							{								
								return_list.add(new StringQuadruple(altered, (base_new + " " + quad.getB()).trim(), subj_Str, subj_Cardinality, depth++));
							}
					}						
				}
			}
		}
		
		return (List<StringQuadruple>) Utils_DuplicateCheck.removeDuplicates(return_list);
	}

	
	/*
	 * Output List of triples
	 * 1 Quadruple = (is altered, relation chain, object, cardinality)
	 */
	public static List<StringQuadruple> getObjectChains(Document doc, Annotation Source_Annot, int depth)
	{
		if(Source_Annot == null) {
			System.out.println("**************");
		}

		Concept_Class object = Utilities.getMapped_NPPrunedString(doc, Source_Annot.getId()); //Get class of the source annotation
		String source = object.getName().trim(); //Source string 
		String cardinality = object.getCardinality().trim(); //Source cardinality (A decision to have the cardinality of the chain as the cardinality of the source)
		String altered = "false"; //This is a bool to tell you that chain is yet unaltered 
		
		List<Annotation> list_chains = Utilities.isChainSource(doc, Source_Annot.getId()); //List of all the chains with Source_Annot as source
		String chain = "";
		String final_concept = "";
		List<StringQuadruple> return_list = new ArrayList<StringQuadruple>();  //Return List
		return_list.add(new StringQuadruple(altered, "", source, cardinality, depth++));
		
		/*
		 * Return blank chain string if there is no chain from this source
		 */
		if(list_chains.size() == 0)
		{
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

			if(list_chains.size() > 1) {
				list_chains = list_chains.subList(0,1);
			}
			
			
			for(Annotation chain_NP: list_chains)
			{
				FeatureMap chainFeatures = chain_NP.getFeatures();
				if(chainFeatures.get("kind").toString().equals("NP_NP") && chainFeatures.get("string").toString().contains(source))
				{
					String PP = chainFeatures.get("relation_Type").toString().replaceAll("prep(c)?_", "").replaceAll("_", " ").trim();

					chain = source + " " + PP + " " + chainFeatures.get("target_String").toString().trim();
					final_concept = chainFeatures.get("target_String").toString().trim();
					
					//cardinality = chainFeatures.get("cardinality").toString();
					altered = "true";
					
					if(prev_PP.equals("")) //First chain
					{
						/*
							Tuple
							A : chain without final_concept
							B : final_concept
						 */
						StringTuple tuple = Utilities.getRelation_Target(chain + "###" + final_concept);
						return_list.add(new StringQuadruple(altered, tuple.getA(), tuple.getB(), cardinality, depth++));
						base_old = source;
						base_new = chain;
					}					
					else if(prev_PP.equals(PP)) //Chain with same prepositions, e.g., "confirmation from the user FOR (this action) and (subsequent related actions)"
					{												
						return_list.add(new StringQuadruple(altered, base_old + " " + PP, final_concept, cardinality, depth++));
						base_new = (base_old + " " + PP + " " + chainFeatures.get("target_String").toString()).trim();
					}
					else //Chain with different continuous prepositions, e.g., "confirmation FROM the user FOR this action"
					{
						base_old = base_new;
						base_new = (base_new + " " + PP + " " + chainFeatures.get("target_String").toString()).trim();
						final_concept = chainFeatures.get("target_String").toString();
						if(source.equals(final_concept)) {
							return return_list;
						}
						return_list.add(new StringQuadruple(altered, base_new.replace(final_concept, ""), final_concept, cardinality, depth++));
					}
					prev_PP = PP;

					if(source.equals(final_concept)) {
						return return_list;
					}

					for(StringQuadruple quad: getObjectChains(doc, doc.getAnnotations().get(Integer.parseInt(chainFeatures.get("target_ID").toString())), depth+1)) //RECURSION
					{
						if(quad.getA() == "true")
							{								
								return_list.add(new StringQuadruple(altered, (source + " " + PP + " " + quad.getB()).trim(), quad.getC(), quad.getD(), depth++));
							}
					}				
				}		
			}
		}
		return_list.sort(Comparator.comparing(StringQuadruple::getB));

		return (List<StringQuadruple>) Utils_DuplicateCheck.removeDuplicates(return_list);
	}
	
	
	/*
	 * Output : List of triples
	 * 1 Quadruple: (PP, relation chain, iobj, iobj cardinality)
	 */
	public static List<StringQuadruple> traverseVP_Chains(Document doc, Annotation relation, int depth)
	{
		AnnotationSet chains_VP = gate.Utils.getOverlappingAnnotations(doc.getAnnotations(), relation, "Chain_1");
		String verb = Utils.cleanStringFor(doc, relation);
		List<Annotation> list_Chains = Utils.inDocumentOrder(chains_VP);
		List<StringQuadruple> return_list = new ArrayList<StringQuadruple>();
		String verbStr = relation.getFeatures().get("str").toString();

		verb = verb.replace("want to", "").replace("wants to", "").replace("able to", "").replace("automatically", "").trim();
		
		return_list.add(new StringQuadruple("", verb, "", "0", depth++));
		
		if(list_Chains.size() == 0)
		{			
			return return_list;
		}
		else if(list_Chains.size() > 0)
			{

			String prev_PP = "";
			String base_new = "";
			
				for(Annotation chain_VP: list_Chains)
				{
					FeatureMap chainFeatures = chain_VP.getFeatures();
					if(chainFeatures.get("kind").toString().equals("VP_NP") && (chainFeatures.get("source_String").toString().equals(verb)))
					{
						String PP = chainFeatures.get("relation_Type").toString().replaceAll("prep(c)?_", "").replaceAll("_", " ");
						String iobj = chainFeatures.get("target_String").toString().trim();
						
						//if(prev_PP.equals(""))//First chain
						//{
							Annotation iobj_annot = doc.getAnnotations().get(Utilities.getMapped_NP(doc, Integer.parseInt(chainFeatures.get("target_ID").toString())));
							List<StringQuadruple> iobj_quads = getObjectChains(doc, iobj_annot, 0);
							iobj_quads.sort(Comparator.comparing(StringQuadruple::getDepth));
							for(StringQuadruple iobj_quad: iobj_quads)
							{
								if(chainFeatures.get("target_category").equals("JJ")) {
									return_list.add(new StringQuadruple("",verbStr + " " + PP + " " +  iobj_quad.getC(), "", "JJ", depth++));
								}
								else if(iobj_quad.getA().equals("true"))
								{						
									return_list.add(new StringQuadruple(/*PP*/"", ((prev_PP.equals("") ? verbStr : base_new) + " " + PP + " " + iobj_quad.getB()).trim(), iobj_quad.getC(), iobj_quad.getD(), depth++));
								}
								else
								{
									Concept_Class iObject = Utilities.getMapped_NPPrunedString(doc, iobj_annot.getId());
									return_list.add(new StringQuadruple(PP, verbStr, iObject.getName(), iObject.getCardinality(), depth++));
								}
							}			
							base_new = verbStr + " " + PP + " " +  iobj;
						//}
						/*else
						{
							Annotation iobj_annot = doc.getAnnotations().get(Utilities.getMapped_NP(doc, Integer.parseInt(chainFeatures.get("target_ID").toString())));
							List<StringQuadruple> iobj_quads = getObjectChains(doc, iobj_annot, 0);
							iobj_quads.sort(Comparator.comparing(StringQuadruple::getDepth));
							for(StringQuadruple iobj_quad: iobj_quads)
							{
								if(iobj_quad.getA().equals("true"))
								{						
									return_list.add(new StringQuadruple("", (base_new + " " + PP + " " + iobj_quad.getB()).trim(), iobj_quad.getC(), iobj_quad.getD(), depth++));
								}
								else
								{
									Concept_Class iObject = Utilities.getMapped_NPPrunedString(doc, iobj_annot.getId());
									return_list.add(new StringQuadruple(PP, base_new, iObject.getName(), iObject.getCardinality(), depth++));
								}
							}			
							base_new = base_new + " " + PP + " " +  iobj;
						}*/
						prev_PP = PP;						
						//Level 2 chaining for VP - NP - NP
						/*for(StringQuadruple quad: getObjectChains(doc, doc.getAnnotations().get(Integer.parseInt(chainFeatures.get("target_ID").toString())), depth+1)) //RECURSION
						{
							if(quad.getA() == "true") {
								return_list.add(new StringQuadruple("", ((prev_PP.equals("") ? verbStr : base_new) + " " + PP + " " + quad.getB()).trim(), quad.getC(), quad.getD(), depth++));
							}
							else {
								Concept_Class iObject = Utilities.getMapped_NPPrunedString(doc, iobj_annot.getId());
								return_list.add(new StringQuadruple(PP, verbStr, iObject.getName(), iObject.getCardinality(), depth++));
							}
						}*/
					}
				}
			}
		return (List<StringQuadruple>) Utils_DuplicateCheck.removeDuplicates(return_list);
	}
}
