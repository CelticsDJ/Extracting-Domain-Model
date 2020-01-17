package data;

public class Concept_Class {
	
	
	String cardinality;
	String name;
	int Id;
	
	public Concept_Class(String class_name,  int Id, String cardinality)
	{
		this.cardinality = cardinality;
		this.name = class_name;
		this.Id = Id;
	}
	
	public Concept_Class(String class_name)
	{
		this.cardinality = "0";
		this.name = class_name;
		this.Id = 0;
	}
	
	
	public String getCardinality()
	{
		return this.cardinality;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getID()
	{
		return Id;
	}

}
