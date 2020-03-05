package processing;

import data.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class Concept_Pattern {

    public static void Concept_Pattern() {

        HashMap<String, Integer> ConceptMap = new HashMap<>();

        Integer req_Id = 1;
        while(req_Id <= ExtractRelations_includingChains.hashmap_requirmenets_Relations.size()) {

            for (Requirement_Relations req_relations : ExtractRelations_includingChains.hashmap_requirmenets_Relations) {
                if (!req_relations.Req_Id.equals("R" + req_Id.toString())) {
                    continue;
                }
                req_Id++;

                if(req_relations.relations == null) {
                    continue;
                }
                Iterator it = req_relations.relations.iterator();

                while(it.hasNext()) {
                    Object obj = it.next();
                    if (!obj.getClass().toString().contains("Concept_Relation")) {

                        Association_Relation rel = (Association_Relation) obj;

                        try {
                            ConceptMap.replace(rel.getSource().getName(), ConceptMap.get(rel.getSource().getName())+1);
                        }catch(NullPointerException e) {
                            ConceptMap.put(rel.getSource().getName(), 1);
                        }

                        try {
                            ConceptMap.replace(rel.getTarget().getName(), ConceptMap.get(rel.getTarget().getName())+1);
                        }catch (NullPointerException e) {
                            ConceptMap.put(rel.getTarget().getName(), 1);
                        }

                    }
                }
            }
        }

        Iterator it = ConceptMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = entry.getKey().toString();
            Integer val = (Integer) entry.getValue();

            if(val == 1) {
                for (Requirement_Relations req_relations : ExtractRelations_includingChains.hashmap_requirmenets_Relations) {

                    if(req_relations.relations == null) {
                        continue;
                    }

                    Iterator req_it = req_relations.relations.iterator();
                    while (req_it.hasNext()) {
                        Object obj = req_it.next();
                        if(!obj.getClass().toString().contains("Concept_Relation")) {

                            Association_Relation rel = (Association_Relation) obj;

                            if(rel.getSource().getName().equals(key) || rel.getTarget().getName().equals(key)) {
                                String rel_str = rel.getRelationName();

                                for(String aggregation_pattern : Aggregation_Patterns.Aggregation_Patterns) {
                                    if(rel_str.equals(aggregation_pattern)) {
                                        rel.setDuplicateStatus(true);
                                        addRelation(rel, RelationType.AGGREGATION, req_relations.Req_Id);
                                    }
                                }

                                for(String generalization_pattern : Generalization_Patterns.Generalization_Patterns) {
                                    if(rel_str.equals(generalization_pattern)) {
                                        rel.setDuplicateStatus(true);
                                        addRelation(rel, RelationType.GENERALIZATION, req_relations.Req_Id);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

    }

    private static void addRelation(Association_Relation old_rel, RelationType relationType, String reqId) {
        Concept_Relation rel = new Concept_Relation(old_rel.getSource(), old_rel.getTarget(), relationType, "N1");
        HashSet<Concept_Relation> relations = ExtractRelations_includingChains.hashmap_reqId_Relations.get("R" + reqId);

        if(relations == null)
        {
            relations = new HashSet<>();
        }

        if(rel.getSource().getName().trim().isEmpty() || rel.getTarget().getName().trim().isEmpty())
        {
            return;
        }
        rel.setSequence_Num(0);
        relations.add(rel);
        ExtractRelations_includingChains.hashmap_reqId_Relations.put("R"+reqId, relations);
    }

}
