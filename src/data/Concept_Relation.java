package data;

import java.util.Objects;

public class Concept_Relation {

	Concept_Class src_ConceptClass;
	Concept_Class target_ConceptClass;
	RelationType rel_type;
	String rule;
	int sequence_num;
	boolean duplicate = false;
	
	public Concept_Relation(Concept_Class src, Concept_Class trgt, RelationType rel, String rule)
	{
		src_ConceptClass = src;
		target_ConceptClass = trgt;
		rel_type = rel;
		this.rule = rule;
	}
	
	public Concept_Class getSource()
	{
		return src_ConceptClass;		
	}
	
	public Concept_Class getTarget()
	{
		return 	target_ConceptClass;		
	}
	
	public RelationType getRelationType()
	{
		return rel_type;
	}
	
	public void setSequence_Num(int num)
	{
		sequence_num = num;
	}
	
	public void setDuplicateStatus(boolean dup)
	{
		duplicate = dup;
	}
	
	public boolean getDuplicateStatus()
	{
		return duplicate;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Concept_Relation))return false;
	    Concept_Relation otherMyClass = (Concept_Relation)other;
	    boolean srcMatch = this.src_ConceptClass.getName().toLowerCase().equals(otherMyClass.getSource().getName().toLowerCase());
	    boolean targetMatch = this.target_ConceptClass.getName().toLowerCase().equals(otherMyClass.getTarget().getName().toLowerCase());
	    boolean relMatch = this.rel_type.equals(otherMyClass.rel_type);
	    return srcMatch & targetMatch & relMatch;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(this.src_ConceptClass.getName().toLowerCase().trim(), this.target_ConceptClass.getName().toLowerCase().trim(), this.rel_type);		
	}
	
}
