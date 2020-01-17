package test;

import data.GlobalVariables;
import junit.framework.TestCase;

import static utils.DeriveAnnotations.isAtomicNP;

public class test_isAtomicNP extends TestCase {

    public void test_isAtomic() {

        System.out.println(isAtomicNP(GlobalVariables.annotated_doc.getAnnotations().get(Integer.parseInt("3357"))));
        /*
        FeatureMap the_chains_features = Factory.newFeatureMap();
        the_chains_features.put("ID", "3357");
        the_chains_features.put("cat", "NP");
        the_chains_features.put("consists", "[3355, 3356]");
        the_chains_features.put("text", "the chains");

        FeatureMap the_features = Factory.newFeatureMap();
        the_features.put("ID", "3355");
        the_features.put("cat", "DT");
        the_features.put("text", "the");
        */
    }

}
