package gate.stanford;

import utils.DeriveAnnotations;

import java.io.Serializable;

public class DependencyRelation implements Serializable {
    private static final long serialVersionUID = -7842607116149222052L;

    /**
     * The type of the dependency relation (det, amod, etc.).
     */
    private String type;

    /**
     * The ID of the token that is the target of this relation.
     */
    private Integer targetId;

    public DependencyRelation(String type, Integer targetId) {
        this.type = type;
        this.targetId = targetId;
    }

    /*public DependencyRelation(String dep) {
        int start = 0, end = 0;
        for (int i = 0; i < dep.length(); i++) {
            if (dep.charAt(i) == '(') {
                start = i;
            }
            if (dep.charAt(i) == ')') {
                end = i;
            }
        }
        String type = dep.substring(0, start), target = dep.substring(start+1, end);
        this.type = type;
        this.targetId = DeriveAnnotations.convertId(Integer.parseInt(target));
    }*/

    /**
     * Return the dependency tag (type).
     *
     * @return the dependency tag
     */
    public String getType() {
        return type;
    }

    /**
     * Set the dependency tag.
     *
     * @param type
     *          dependency tag
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Return the GATE Annotation ID of the dependent.
     *
     * @return the Annotation ID
     */
    public Integer getTargetId() {
        return targetId;
    }

    /**
     * Set the Annotation ID of the dependent.
     *
     * @param targetId
     *          the Annotation ID
     */
    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    /**
     * Format the data structure for display. For example, if type is "dobj" and
     * the dependent has Annotation ID 37, return the String "dobj(37)".
     */
    public String toString() {
        return type + "(" + targetId + ")";
    }
}
