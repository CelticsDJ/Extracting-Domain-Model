package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils_DuplicateCheck {
	
	public static List<StringQuadruple> removeDuplicates(List<StringQuadruple> list)
	{
		HashMap<StringTuple, StringQuadruple> map = new HashMap<>();
		for(StringQuadruple quad: list)
		{
			map.put(new StringTuple(quad.getB(), quad.getC()), quad);
		}		
		List<StringQuadruple> return_list = new ArrayList<StringQuadruple>(map.values());
		return return_list;		
	}
	
	public static void conceptRelationDuplicates()
	{
		
	}

}
