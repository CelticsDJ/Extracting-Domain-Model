package data;

import java.util.ArrayList;
import java.util.List;

import utils.StringQuadruple;
import utils.Utils_DuplicateCheck;

public class MetaRelation {
	
	int relation_Id;
	List<StringQuadruple> subject_quads;
	List<StringQuadruple> object_quads;
	List<StringQuadruple> relation_chains;
	int advcl_metaRelation;
	
	public MetaRelation(int relation_Id)
	{
		this.relation_Id = relation_Id;
		subject_quads = new ArrayList<StringQuadruple>();
		object_quads = new ArrayList<StringQuadruple>();
		relation_chains = new ArrayList<StringQuadruple>();
	}
	
	public void setSubjects(List<StringQuadruple> subject_quads)
	{
		this.subject_quads = subject_quads;
	}
	
	public void setRelationChain(List<StringQuadruple> relation_quads)
	{
		this.relation_chains = relation_quads;
	}
	
	public void setObjects(List<StringQuadruple> object_quads)
	{
		this.object_quads.addAll(object_quads);
		this.object_quads = Utils_DuplicateCheck.removeDuplicates(this.object_quads);
	}
	
	public List<StringQuadruple> getSubjects()
	{
		return subject_quads;
	}
	
	public List<StringQuadruple> getObjects()
	{
		return object_quads;
	}

	public List<StringQuadruple> getRelationChains()
	{
		return relation_chains;
	}
}
