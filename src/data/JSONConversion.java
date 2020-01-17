package data;

import com.google.gson.Gson;

import processing.ExtractRelations_includingChains;

public class JSONConversion {
	
	public static String converttoJSON()
	{
		Gson gson = new Gson();		
		return gson.toJson(ExtractRelations_includingChains.hashmap_requirmenets_Relations);
	}

}
