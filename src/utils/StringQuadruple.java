package utils;

public class StringQuadruple {
    private String a, b, c, d;

    public StringQuadruple (String a, String b, String c, String d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
    }

    public String getA() {
      return a;
    }

    public String getB() {
      return b;
    }

    public String getC() {
      return c;
    }
   
    public String getD() {
        return d;
      }

    public void setA(String a) {
      this.a = a;
    }

    public void setB(String b) {
      this.b = b;
    }

    public void setC(String c) {
      this.c = c;
    }
    
    public void setD(String d) {
        this.d = d;
      }      

    public String toString() {
      return "(" + a.toString() + "," + b.toString() + "," + c.toString() + ")";
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }

        if (anObject instanceof StringQuadruple) {
            StringQuadruple quad = (StringQuadruple) anObject;
            if (this.getA().equals(quad.getA()) && this.getB().equals(quad.getB()) && this.getC().equals(quad.getC()) && this.getD().equals(quad.getD())) {
                return true;
            }
        }

        return false;
    }

}
