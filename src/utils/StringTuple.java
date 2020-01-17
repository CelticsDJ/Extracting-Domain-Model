package utils;

public class StringTuple {
    private String a, b;
    private int hash;

    public StringTuple (String a, String b) {
      this.a = a;
      this.b = b;
      this.hash = calculateHashCode();
    }

    public String getA() {
      return a;
    }

    public String getB() {
      return b;
    }

    public void setA(String a) {
      this.a = a;
    }

    public void setB(String b) {
      this.b = b;
    }

    public String toString() {
      return "(" + a.toString() + "," + b.toString() + ")";
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }

        if (anObject instanceof StringTuple) {
            StringTuple tuple = (StringTuple) anObject;
            if (this.getA().equals(tuple.getA()) && this.getB().equals(tuple.getB())) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        return hash;
    }

    private int calculateHashCode() {
        return (a+b).hashCode();
    }
}
