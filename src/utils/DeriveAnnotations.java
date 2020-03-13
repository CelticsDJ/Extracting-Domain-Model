package utils;

import data.GlobalVariables;
import gate.*;
import gate.stanford.DependencyRelation;
import org.bouncycastle.jcajce.provider.drbg.DRBG;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class DeriveAnnotations {

    private static AnnotationSet annotations;
    private static List<Annotation> tokens = new ArrayList<>();
    //private static ArrayList<Annotation> STNs = new ArrayList<>();
    //private static ArrayList<Annotation> VBs = new ArrayList<>();
    private static List<Annotation> NPs = new ArrayList<>();
    //private static ArrayList<Annotation> NNs = new ArrayList<>();
    //private static ArrayList<Annotation> VPs = new ArrayList<>();
    //private static ArrayList<Annotation> STN_NNs = new ArrayList<>();
    private  static List<Annotation> Parse_NPs = new ArrayList<>();

    public static void DeriveAnnotations() {

        init();

        //Derive Parse_NP
        tokens = annotations.get("Token").inDocumentOrder();
        NPs = annotations.get("NP").inDocumentOrder();
        Parse_NPs = annotations.get("Parse_NP").inDocumentOrder();

        for(Annotation NP : NPs) {
            //已有，不处理
            if(NP.getId() != Utilities.getMapped_NP(GlobalVariables.annotated_doc, NP.getId())){
                //System.out.println(NP.getStartNode().getOffset() + " " + NP.getEndNode().getOffset());
                continue;
            }

            //没有，或者覆盖原先
            else {
                List<Integer> consists = Utilities.getTokenID(GlobalVariables.annotated_doc, NP.getId());

                String firstToken = annotations.get(consists.get(0)).getFeatures().get("string").toString();

                if(firstToken.toLowerCase().equals("which") || firstToken.toLowerCase().equals("that")) {
                    continue;
                }

                else {
                    FeatureMap featureMap = Factory.newFeatureMap();

                    Node start = annotations.get(consists.get(0)).getStartNode();
                    Node end = annotations.get(consists.get(consists.size()-1)).getEndNode();

                    List<DependencyRelation> dependencies = new ArrayList<>();
                    for (Integer id : consists) {
                        List<DependencyRelation> tmp = (List<DependencyRelation>) annotations.get(id).getFeatures().get("dependencies");
                        if(tmp != null) {
                            for (DependencyRelation dep : tmp) {
                                dependencies.add(dep);
                            }
                        }
                    }

                    // Stop words removal(fake)
                    String pruned_string = "";
                    String root = "";
                    for(Integer id : consists) {
                        String tmp = annotations.get(id).getFeatures().get("root").toString();
                        String tmp_root = annotations.get(id).getFeatures().get("root").toString();
                        if(!tmp.toLowerCase().equals("the") && !tmp.toLowerCase().equals("any") && !tmp.toLowerCase().equals("s") && !tmp.toLowerCase().equals("'s")) {
                            pruned_string = pruned_string.concat(tmp + " ");
                        }
                        root = root.concat(tmp_root + " ");
                    }
                    /*if (pruned_string.startsWith("The") || pruned_string.startsWith("the") || pruned_string.startsWith("Any") || pruned_string.startsWith("any")) {
                        pruned_string = pruned_string.substring(4);
                    }*/

                    String structure = "";
                    String pruned_structure = "";
                    for (Integer id : consists) {
                        String category = annotations.get(id).getFeatures().get("category").toString();
                        if(!category.equals("DT") && !category.equals("PDT") && !category.equals("POS")) {
                            pruned_structure = pruned_structure.concat(category);
                            pruned_structure = pruned_structure.concat("-");
                        }
                        structure = structure.concat(category);
                        structure = structure.concat("-");
                    }

                    String isPlural = "false";
                    for (Integer id : consists) {
                        String category = annotations.get(id).getFeatures().get("category").toString();
                        if (category.equals("NNS") || category.equals("NNPS") || category.equals("NPS")) {
                            isPlural = "true";
                            break;
                        }
                    }

                    featureMap.put("dependencies", dependencies);
                    featureMap.put("firstToken", firstToken);
                    featureMap.put("isPlural", isPlural);
                    featureMap.put("pruned_string", pruned_string.trim());
                    featureMap.put("pruned_structure", pruned_structure);
                    featureMap.put("root", root);
                    featureMap.put("structure", structure);
                    featureMap.put("validNN", "true"); //not check

                    List<Integer> Overlapping_NPs = Utilities.getOverlappingNPs(GlobalVariables.annotated_doc, NP.getId());
                    if(Overlapping_NPs.size() != 0) {
                        for(Integer id : Overlapping_NPs) {
                            Annotation a = annotations.get(id);
                            annotations.remove(a);
                        }
                    }

                    annotations.add(start, end, "Parse_NP", featureMap);

                }
            }
        }


        //Relation_Verb
        //for (Annotation a : VBs) {

            //Annotation a : VPs

            /*
            List<Integer> consists = (ArrayList<Integer>) a.getFeatures().get("consists");
            FeatureMap featureMap = a.getFeatures();
            List<String> dependencies = new ArrayList<>();

            for(Integer id : consists) {

                Annotation tmp = annotations.get(id);

                if(tmp.getFeatures().get("cat").toString().startsWith("VB")){
                    featureMap.put("root", tmp.getFeatures().get("text"));
                }

                List<String> deps = (List<String>) tmp.getFeatures().get("dependencies");
                if(deps != null) {
                    for(String dep : deps) {
                        DependencyRelation dependencyRelation = new DependencyRelation(dep);
                        if(Utilities.getMapped_VP(GlobalVariables.annotated_doc, dependencyRelation.getTargetId()) != a.getId()) {
                            dependencies.add(dep);
                        }
                    }
                }

            }

            featureMap.put("dependencies", dependencies);
            annotations.add(a.getStartNode(), a.getEndNode(), "Relation_Verb", featureMap);
             */


            //Annotation a : VBs

        /*
            FeatureMap featureMap = a.getFeatures();

            //derive prep_*
            List<String> dependencies = (List<String>) a.getFeatures().get("dependencies");
            dependencies = derive_prep(dependencies);

            featureMap.remove("dependencies");
            featureMap.put("dependencies", dependencies);

            annotations.add(a.getStartNode(), a.getEndNode(), "Relation_Verb", a.getFeatures());

        }
        */

        //Parse_NP(Atomic) -dependencies, pruned_string, pruned_structure, validNN, firstToken, isPlural
        /*for (Annotation a : NPs) {

            if (!isAtomicNP(a)) continue;

            FeatureMap featureMap = a.getFeatures();
            List<Integer> consists = (List<Integer>) a.getFeatures().get("consists");

            List<String> dependencies = new ArrayList<>();
            for (Integer id : consists) {
                List<String> tmp = (List<String>) annotations.get(id).getFeatures().get("dependencies");
                if (tmp != null) {
                    dependencies.addAll(tmp);
                }
            }
            dependencies = derive_prep(dependencies);

            featureMap.put("dependencies", dependencies);

            // Stop words removal(fake)
            String pruned_string = (String) a.getFeatures().get("text");
            if (pruned_string.startsWith("The") || pruned_string.startsWith("the") || pruned_string.startsWith("Any") || pruned_string.startsWith("any")) {
                pruned_string = pruned_string.substring(4);
            }

            String pruned_structure = "";
            for (Integer id : consists) {
                pruned_structure.concat((String) annotations.get(id).getFeatures().get("cat"));
                pruned_structure.concat("-");
            }

            String isPlural = "false";
            for (Integer id : consists) {
                if (annotations.get(id).getFeatures().get("cat") == "NNS" || annotations.get(id).getFeatures().get("cat") == "NNPS" || annotations.get(id).getFeatures().get("cat") == "NPS") {
                    isPlural = "true";
                    break;
                }
            }

            featureMap.put("dependencies", dependencies);
            featureMap.put("pruned_string", pruned_string);
            featureMap.put("pruned_structure", pruned_structure);
            featureMap.put("validNN", "true"); //not check
            featureMap.put("firstToken", annotations.get(consists.get(0)).getFeatures().get("text"));
            featureMap.put("isPlural", isPlural);

            annotations.add(a.getStartNode(), a.getEndNode(), "Parse_NP", featureMap);

        }
        */

        //Dep-advmod

        //VP_PP

        //Derive nsubj

    }

    private static void init() {

        //Get default Annotation
        annotations = GlobalVariables.annotated_doc.getAnnotations();
        /*
        //Get Tokens
        for (Annotation a : annotations) {
            if (a.getType().equals("Token")) {
                tokens.add(a);
            }
        }

        //Get SyntaxTreeNodes
        for (Annotation a : annotations) {
            if (a.getType().equals("SyntaxTreeNode")) {
                STNs.add(a);
            }
        }

        //Get VBs from tokens
        for (Annotation a : tokens) {
            if (a.getFeatures().get("category").toString().startsWith("VB")) {

                Annotation STN_VB = annotations.get(convertId(a.getId()));
                FeatureMap featureMap = STN_VB.getFeatures();

                if (a.getFeatures().get("dependencies") == null) {
                    continue;
                } else {
                    featureMap.put("dependencies", a.getFeatures().get("dependencies"));
                }

                if(a.getFeatures().get("stem") == null) {
                    //continue;
                }
                else {
                    featureMap.put("stem", a.getFeatures().get("stem"));
                }

                STN_VB.setFeatures(featureMap);
                VBs.add(STN_VB);
            }
        }

        //Get NPs from SyntaxNodes
        for (Annotation a : STNs) {
            if (a.getFeatures().get("cat").toString().startsWith("NP")) {
                NPs.add(a);
            }
        }

        //Get NNs from tokens
        for (Annotation a : tokens) {
            if (a.getFeatures().get("category").toString().startsWith("NN")) {

                Annotation STN_NN = annotations.get(convertId(a.getId()));
                FeatureMap featureMap = STN_NN.getFeatures();

                featureMap.put("dependencies", a.getFeatures().get("dependencies"));
                featureMap.put("stem", a.getFeatures().get("stem"));

                STN_NN.setFeatures(featureMap);
                NNs.add(STN_NN);
            }
        }

        //Get (atomic) VPs from SyntaxTreeNodes
        for (Annotation a : STNs) {
            if (a.getFeatures().get("cat").equals("VP")) {

                boolean isAtomic = true;

                List<Integer> consists = (ArrayList<Integer>) a.getFeatures().get("consists");
                for (Integer id : consists) {
                    String cat = annotations.get(id).getFeatures().get("cat").toString();
                    if (cat.equals("VP") || cat.equals("S")) {
                        isAtomic = false;
                        break;
                    }
                }
                if (isAtomic) {
                    VPs.add(a);
                }
            }*/

        //Get NNs from SyntaxTreeNodes
        /*for(Annotation a : STNs) {
            if(a.getFeatures().get("cat").equals("NN")) {
                STN_NNs.add(a);
            }
        }*/

    }

    private static boolean isreceivedDep(Annotation a) {

        String[] unreceivedDeps = {"det", "root", "nn", "aux"};
        String dep = a.getFeatures().get("kind").toString();

        for (String s : unreceivedDeps) {
            if (s.equals(dep)) {
                return false;
            }
        }

        return true;
    }

    //Tokens id to SyntaxTreeNodes id
    /*public static Integer convertId(Integer id) {

        Annotation a = annotations.get(id);

        for (Annotation b : STNs) {
            if (b.getStartNode().equals(a.getStartNode()) && b.getEndNode().equals(a.getEndNode())) {
                return b.getId();
            }
        }

        return id;

    }*/

    /*private static Annotation convertNNId(Annotation a) {

        for(Annotation stn : STN_NNs){
            if(stn.getStartNode().equals(a.getStartNode()) && stn.getEndNode().equals(a.getEndNode())) {
                return stn;
            }
        }

        return a;
    }

    private static Annotation NNtoNP(Annotation a) {

        Annotation newNN = convertNNId(a);
        Integer newNNId = newNN.getId();

        for(Annotation NP : NPs) {

            @SuppressWarnings("unchecked")
            ArrayList<Integer> nodes = (ArrayList<Integer>) NP.getFeatures().get("consists");

            for(Integer i : nodes) {
                if(newNNId.equals(i)) {
                    return NP;
                }
            }
        }

        return newNN;

    }*/

    public static boolean isAtomicNP(Annotation a) {

        FeatureMap featureMap = a.getFeatures();
        List<Integer> consists = (List<Integer>) featureMap.get("consists");

        if(consists != null) {

            for (Integer id : consists) {

                Annotation tmp = annotations.get(id);
                if (tmp.getFeatures().get("cat").equals("NP"))
                    return false;

                else if (tmp.getFeatures().get("consists") != null) {

                    List<Integer> tmp_consists = (List<Integer>) tmp.getFeatures().get("consists");
                    boolean flag = true;

                    for (Integer tmp_id : tmp_consists) {
                        flag &= isAtomicNP(annotations.get(tmp_id));
                    }

                    return flag;
                }
            }
        }

        return true;
    }

    private static List<String> derive_prep(List<String> dependencies) {

        List<String> new_prep_deps = new ArrayList<>();
        Pattern p = Pattern.compile("[^0-9]");

        for (String dep : dependencies) {

            if (dep.contains("prep")) {

                String prep_ = "prep_";

                Integer prep_id = Integer.parseInt(p.matcher(dep).replaceAll(""));
                Annotation prep = annotations.get(prep_id);
                prep_ += prep.getFeatures().get("string");
                List<String> prep_deps = (List<String>) prep.getFeatures().get("dependencies");

                if (prep.getFeatures().get("string").equals("according")) {
                    prep_id = Integer.parseInt(p.matcher(prep.getFeatures().get("dependencies").toString()).replaceAll(""));
                    prep = annotations.get(prep_id);
                    prep_ += "_to";
                    prep_deps = (List<String>) prep.getFeatures().get("dependencies");
                }

                for (String prep_dep : prep_deps) {
                    if (prep_dep.contains("pobj")) {
                        Integer prep_target_id = Integer.parseInt(p.matcher(prep_dep).replaceAll(""));
                        prep_ += "(" + prep_target_id.toString() + ")";
                    }
                }

                new_prep_deps.add(prep_);
            } else {
                new_prep_deps.add(dep);
            }
        }

        //dependencies.addAll(new_prep_deps);

        return new_prep_deps;

    }
}
