package data;

import java.util.HashSet;

public class Requirement_Relations {
	
	public String Req_Id;
	public String Req_txt;
	public HashSet<Concept_Relation> relations;
	
	public Requirement_Relations(String id, String text, HashSet<Concept_Relation> relations)
	{
		Req_Id = id;
		Req_txt = text;
		this.relations  = relations;		
	}

}
