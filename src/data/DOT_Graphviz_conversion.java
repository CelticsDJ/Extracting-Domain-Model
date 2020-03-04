package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import processing.ExtractRelations_includingChains;

public class DOT_Graphviz_conversion {
	
	
	public static void writeDOTFile(String fileName)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("digraph G {" + System.lineSeparator()); //Opening Digraph		
		sb.append("fontname = \"Bitstream Vera Sans\"" + System.lineSeparator()); //Font info
        sb.append("fontsize = 8" + System.lineSeparator()); //Font info
        
        sb.append("node [ " + System.lineSeparator()); //NODE settings
        sb.append("fontname = \"Bitstream Vera Sans\"" + System.lineSeparator());
        sb.append("fontsize = 8" + System.lineSeparator());
        sb.append("shape = \"record\"" + System.lineSeparator());
        sb.append("]" + System.lineSeparator());
        
        sb.append("edge [ " + System.lineSeparator()); //EDGE settings
        sb.append("fontname = \"Bitstream Vera Sans\"" + System.lineSeparator());
        sb.append("fontsize = 8" + System.lineSeparator());
        sb.append("]" + System.lineSeparator());

        sb.append("\n\n");

        Integer req_id = 1;
        while(req_id <= ExtractRelations_includingChains.hashmap_requirmenets_Relations.size()) {

			for (Requirement_Relations req_relations : ExtractRelations_includingChains.hashmap_requirmenets_Relations) {
				if (!req_relations.Req_Id.equals("R" + req_id.toString())) {
					continue;
				}
				req_id++;
				sb.append(req_relations.Req_Id + ':' + req_relations.Req_txt + '\n');
				if(req_relations.relations == null) {
					continue;
				}
				Iterator it = req_relations.relations.iterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (obj.getClass().toString().contains("Concept_Relation")) {
						Concept_Relation rel = (Concept_Relation) obj;
						if(!rel.getDuplicateStatus()) {
							if (rel.getRelationType().equals(RelationType.ATTRIBUTE)) {
								sb.append(rel.getSource().name.replace(" ", "_") + " [ label = \"{" + rel.getSource().name + " |+ " + rel.getTarget().name + " : \\l}\"]" + System.lineSeparator());
							} else {
								sb.append(rel.getSource().name.replace(" ", "_") + " [ label = \"{" + rel.getSource().name + " : \\l}\"]" + System.lineSeparator());
								sb.append(rel.getTarget().name.replace(" ", "_") + " [ label = \"{" + rel.getTarget().name + " : \\l}\"]" + System.lineSeparator());
								if (rel.getRelationType().equals(RelationType.AGGREGATION)) {
									sb.append(rel.getSource().name.replace(" ", "_") + " -> " + rel.getTarget().name.replace(" ", "_"));
									sb.append("  [arrowhead = \"odiamond\"]" + System.lineSeparator());
								} else if (rel.getRelationType().equals(RelationType.GENERALIZATION)) {
									sb.append(rel.getSource().name.replace(" ", "_") + " -> " + rel.getTarget().name.replace(" ", "_"));
									sb.append("  [arrowhead = \"empty\"]" + System.lineSeparator());
								}
							}//END ELSE
							sb.append(rel.rule);
						}
					} else {
						Association_Relation rel = (Association_Relation) obj;
						sb.append(rel.getSource().name.replace(" ", "_") + " [ label = \"{" + rel.getSource().name + " : \\l}\"]" + System.lineSeparator());
						sb.append(rel.getTarget().name.replace(" ", "_") + " [ label = \"{" + rel.getTarget().name + " : \\l}\"]" + System.lineSeparator());
						sb.append(rel.getSource().name.replace(" ", "_") + " -> " + rel.getTarget().name.replace(" ", "_"));
						sb.append("  [arrowhead = \"none\", label = \"" + rel.getRelationName() + "\"]" + System.lineSeparator());
						sb.append(rel.rule);
					}
					sb.append('\n');
				}
				sb.append("\n\n");
        	/*
        	for(Concept_Relation rel: relations.relations)
        	{
        		//Add Nodes -- Format CONCEPT_ID [ label = "{CONCEPT_NAME |+ ATTRIBUTE : \l}"]
        		
        	}//END FOR */
			}//END FOR
		}
       
        sb.append("}");
        writeTextFile(fileName, sb.toString());
        
	}//END METHOD
	
	private static void writeTextFile(String fileName, String s) {
	    FileWriter output = null;
	    BufferedWriter writer = null;
	    try {
	      output = new FileWriter(fileName);
	      writer = new BufferedWriter(output);
	      writer.write(s);
	    } catch (Exception e) {
	      throw new RuntimeException(e);
	    } finally {
	      if (output != null) {
	        try {
	          //output.close();
	          writer.close();
	        } catch (IOException e) {
	          // Ignore issues during closing
	        	System.out.println(e.getMessage());
	        }
	      }
	    }

	  }
	
}//END CLASS
