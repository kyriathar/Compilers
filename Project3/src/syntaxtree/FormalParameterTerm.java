//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * f0 -> ","
 * f1 -> FormalParameter()
 */
public class FormalParameterTerm implements Node {
   public NodeToken f0;
   public FormalParameter f1;

   public FormalParameterTerm(NodeToken n0, FormalParameter n1) {
      f0 = n0;
      f1 = n1;
   }

   public FormalParameterTerm(FormalParameter n0) {
      f0 = new NodeToken(",");
      f1 = n0;
   }

   public void accept(visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
}
