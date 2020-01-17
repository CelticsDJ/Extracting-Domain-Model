package test;

import java.util.HashSet;

import junit.framework.TestCase;
import data.Association_Relation;
import data.Concept_Class;

public class test_Assoctaion_Relation extends TestCase{
	
	
	public void testAssociationRelationEquals()
	{
		Concept_Class A1 = new Concept_Class("Class_A", 1, "1");
		Concept_Class B1 = new Concept_Class("Class_B", 1, "1");
		Concept_Class A2 = new Concept_Class("Class_a", 1, "1");
		Concept_Class B2 = new Concept_Class("class_b", 1, "1");
		
		Association_Relation rel1 = new Association_Relation(A1, B1, "depends upon", "B3");
		Association_Relation rel2 = new Association_Relation(A2, B2, "depends upon", "B3");
		Association_Relation rel3 = new Association_Relation(A1, B1, "depends upon it", "B2");
		Association_Relation rel4 = new Association_Relation(A1, B2, "Depends upon", "B2");
		
		assertEquals(true, rel1.equals(rel1));
		assertEquals(true, rel1.equals(rel2));
		assertEquals(false, rel1.equals(rel3));
		assertEquals(true, rel1.equals(rel4));
		
		HashSet<Association_Relation> set = new HashSet<Association_Relation>();
		set.add(rel1);
		set.add(rel2);
		set.add(rel3);
		set.add(rel4);
		assertEquals(2, set.size());

	}

}
