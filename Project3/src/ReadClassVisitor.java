import mapping.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

import javax.lang.model.type.NullType;
import java.util.Map;

/**
 * Created by hduser on 8/4/2016.
 */
public class ReadClassVisitor extends GJDepthFirst<Integer, MySymbolTable> {   //to arg tha  allaksei se domi
    MySymbol mySymbol ;
    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public Integer visit(Goal n, MySymbolTable myTable) {
        String symbolName = n.f0.f1.f0.toString() ;         //Main class name
        mySymbol = new Cclass(symbolName);
        myTable.insert(symbolName,mySymbol);
        if(n.f1.present()) {                                //exei TypeDeclaration
            n.f1.accept(this,myTable);
        }
        return null;
    }
    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public Integer visit(TypeDeclaration n,  MySymbolTable myTable) {
        return n.f0.accept(this, myTable);
    }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public Integer visit(ClassDeclaration n, MySymbolTable myTable) {
        String symbolName = n.f1.f0.toString() ;                //No extend class name
        mySymbol = new Cclass(symbolName);
        myTable.insert(symbolName,mySymbol);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public Integer visit(ClassExtendsDeclaration n, MySymbolTable myTable) {
        String symbolName = n.f1.f0.toString() ;                //Extend class name
        String motherClass = n.f3.f0.toString() ;
        mySymbol = new Cclass(symbolName);
        ((Cclass)mySymbol).setmotherClass(motherClass);
        myTable.insert(symbolName,mySymbol);
        return null;
    }
}
