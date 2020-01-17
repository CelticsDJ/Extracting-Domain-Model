package data;

import java.util.HashSet;

public class Requirement_Relations {
	
	String Req_Id;
	String Req_txt;
	HashSet<Concept_Relation> relations;
	
	public Requirement_Relations(String id, String text, HashSet<Concept_Relation> relations)
	{
		Req_Id = id;
		Req_txt = text;
		this.relations  = relations;		
	}

}
