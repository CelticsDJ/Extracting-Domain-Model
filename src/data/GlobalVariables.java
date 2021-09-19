package data;

import gate.Document;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariables {
	
	public static Document annotated_doc;

	public static Map<String, Integer> conceptCnt;
	
	public static void setAnnotatedDoc(Document doc)
	{
		annotated_doc = doc;
	}

}
