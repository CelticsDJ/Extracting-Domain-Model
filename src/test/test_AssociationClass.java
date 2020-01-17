package test;

import junit.framework.TestCase;
import data.Concept_Class;

public class test_AssociationClass extends TestCase {
	
	public void testAssociationRelationEquals()
	{
		Concept_Class A1 = new Concept_Class("Class_A", 1, "1");
		Concept_Class B1 = new Concept_Class("Class_B", 1, "1");
		Concept_Class A2 = new Concept_Class("Class_a", 1, "1");
		Concept_Class B2 = new Concept_Class("class_b", 1, "1");
		
		
		assertEquals(true, A1.getName().equals("Class_A"));
		assertEquals(true, B1.getName().equals("Class_B"));
		assertEquals(true, A2.getName().equals("Class_a"));
		assertEquals(true, B2.getName().equals("class_b"));
		assertEquals(false, A1.getName().equals("Class_b"));
		
	}


}
