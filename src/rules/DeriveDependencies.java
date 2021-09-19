package rules;

import data.GlobalVariables;
import gate.*;
import gate.stanford.DependencyRelation;

import java.util.List;

public class DeriveDependencies {

    public  static void DeriveDependencies() {
        Document annotatedDoc = GlobalVariables.annotated_doc;
        AnnotationSet Relation_Verb = annotatedDoc.getAnnotations().get("Relation_Verb");
        List<Annotation> VPs = Utils.inDocumentOrder(Relation_Verb);

        for (Annotation VP : VPs) {

            FeatureMap VP_features = VP.getFeatures();
            List<DependencyRelation> list_dependencies = (List<DependencyRelation>) VP_features.get("dependencies");

            for (DependencyRelation dependency : list_dependencies) {
                if (dependency.getType().equals("conj")) {

                }
            }
        }
    }
}
