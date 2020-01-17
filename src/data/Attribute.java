package data;

public class Attribute {

	String name;
	int Id;
	
	public Attribute(String attr_name, int attr_id)
	{
			name = attr_name;
			Id = attr_id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getId()
	{
		return this.Id;
	}
	
}
