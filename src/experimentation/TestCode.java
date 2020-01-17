package experimentation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;

//import edu.stanford.nlp.ling.CoreAnnotations.GenericTokensAnnotation;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;

public class TestCode {
	
	public static void main(String [] args)
	{
		AnnotationSet as = null;
		FeatureMap features = Factory.newFeatureMap();
		for(Object keys: features.keySet())
		{
			
		}
		//gate.Utils.stringFor(arg0, arg1, arg2)
		Annotation annot;
		//annot.getFeatures().get(key)
		//gate.Utils.stringFor(arg0, arg1)
		//gate.Utils.getcon
		//annot.toString().toLowerCase()
		//gate.Utils.get
		//(s)
		Exception e;
		FeatureMap feature = Factory.newFeatureMap();
		//feature.
		List<Integer> lst = new ArrayList<Integer>(); 
		
		String str = "[nsubj(64638), aux(64640), dobj(64646), advmod(64656)]";
		String pattern = "advmod\\((\\d+)";
		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(str);
	      if (m.find( )) {
	         System.out.println("Found value: " + m.group(0) );
	         System.out.println("Found value: " + m.group(1) );
	         //System.out.println("Found value: " + m.group(2) );
	      } else {
	         System.out.println("NO MATCH");
	      }
		
	      String rel = "prep_according_to";
	      rel = rel.replaceAll("prep(c)?_", "");
	      System.out.println(rel);
	      
	        
	    		  
	    		  //getAnnotationsAtOffset(inputAS, tok.getEndNode().getOffset() + 1).iterator().next();
	      
	}

}
