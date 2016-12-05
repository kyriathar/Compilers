//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * f0 -> "HALLOCATE"
 * f1 -> SimpleExp()
 */
public class HAllocate implements Node {
   public NodeToken f0;
   public SimpleExp f1;

   public HAllocate(NodeToken n0, SimpleExp n1) {
      f0 = n0;
      f1 = n1;
   }

   public HAllocate(SimpleExp n0) {
      f0 = new NodeToken("HALLOCATE");
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

