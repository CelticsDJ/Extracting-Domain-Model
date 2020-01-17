package data;

import java.util.Objects;

public class Association_Relation extends Concept_Relation {

	Concept_Class src_Class;
	Concept_Class target_Class;
	String rel_name;
	
	
	public Association_Relation(Concept_Class src, Concept_Class trgt, String rel, String rule)
	{
		super(src, trgt, RelationType.ASSOCIATION, rule);
		src_Class = src;
		target_Class = trgt;
		rel_name = rel;
	}
	
	public Concept_Class getSource()
	{
		return src_Class;		
	}
	
	public Concept_Class getTarget()
	{
		return 	target_Class;		
	}
	
	public String getRelationName()
	{
		return rel_name;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Association_Relation))return false;
	    Association_Relation otherMyClass = (Association_Relation)other;
	    boolean srcMatch = this.src_Class.getName().toLowerCase().trim().equals(otherMyClass.getSource().getName().toLowerCase().trim());
	    boolean targetMatch = this.target_Class.getName().toLowerCase().trim().equals(otherMyClass.getTarget().getName().toLowerCase().trim());
	    boolean relMatch = this.rel_name.toLowerCase().trim().equals(otherMyClass.rel_name.toLowerCase().trim());
	    return srcMatch & targetMatch & relMatch;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(this.src_Class.getName().toLowerCase().trim(), this.target_Class.getName().toLowerCase().trim(), this.rel_name.toLowerCase().trim());		
	}
	
}
