import mapping.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

/**
 * Created by hduser on 9/4/2016.
 */
public class ReadVarsFuncVisitor extends GJDepthFirst<Integer, MySymbolTable> {

    private boolean UserArgType = false;
    private MySymbol myFuncPtr = null;
    private MySymbol mySymbol = null;
    private MySymbol argSymbol = null ;
    private boolean argVal = false ;
    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public Integer visit(Goal n, MySymbolTable myTable) {
        String symbolName = n.f0.f1.f0.toString() ;                     //Main class name
        n.f0.accept(this,myTable.enter(symbolName));                    //enter MainClass - f0 scope
        if(n.f1.present()){
            n.f1.accept(this,myTable);
        }
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public Integer visit(MainClass n, MySymbolTable myTable) {
        mySymbol = new Function() ;
        myFuncPtr = mySymbol;
        mySymbol.setReturnType("void");
        mySymbol.setName("main");
        ((Function)mySymbol).addArg("String[]");
        myTable.insert(Function.WrapFunc(mySymbol.getName()), mySymbol);
        mySymbol = null;

        String wrappedFuncName = Function.WrapFunc(myFuncPtr.getName());
        MySymbolTable myFuncTable =myTable.enter(wrappedFuncName);

        mySymbol = new Var() ;      //insert main's args
        mySymbol.setMySymbolTable(null);
        mySymbol.setReturnType("String[]");
        mySymbol.setName(n.f11.f0.toString());
        myFuncTable.getMap().put(n.f11.f0.toString(),mySymbol);
        mySymbol = null ;

        if(n.f14.present()){
            n.f14.accept(this,myFuncTable);
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
        String symbolName = n.f1.f0.toString() ;                //class name
        if(n.f3.present()){                                     //=> uparxei Data member
            n.f3.accept(this,myTable.enter(symbolName));        //enter Class - f1 scope
        }
        if(n.f4.present()){                                     //Methods
            n.f4.accept(this,myTable.enter(symbolName));        //enter Class - f1 scope
        }
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
        String symbolName = n.f1.f0.toString() ;                //class name
        if(n.f5.present()){                                     //=> uparxei Data member
            n.f5.accept(this,myTable.enter(symbolName));        //enter Class - f1 scope
        }
        if(n.f6.present()){                                     //Methods
            n.f6.accept(this,myTable.enter(symbolName));        //enter Class - f1 scope
        }
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public Integer visit(VarDeclaration n, MySymbolTable myTable) {
        mySymbol = new Var() ;
        n.f0.accept(this, myTable);
        n.f1.accept(this, myTable);
        myTable.insert(mySymbol.getName(), mySymbol);           //telos mias dilwsis metavlitis -> eisagwgi ston pinaka
        mySymbol = null ;
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public Integer visit(MethodDeclaration n, MySymbolTable myTable) {
        mySymbol = new Function() ;
        myFuncPtr = mySymbol;
        n.f1.accept(this, myTable);
        n.f2.accept(this, myTable);
        myTable.insert(Function.WrapFunc(mySymbol.getName()), mySymbol);                //PROSOXI : WRAPAREIS MONO TO KEY TOU HASHMAP
        mySymbol = null;

        String wrappedFuncName = Function.WrapFunc(myFuncPtr.getName());
        MySymbolTable myFuncTable =myTable.enter(wrappedFuncName);
        if(n.f4.present()){     //exoume arguments
            n.f4.accept(this,myFuncTable);              //enter function's scope
        }

        if(n.f7.present()){             //Exoume Variable
            n.f7.accept(this,myFuncTable);
        }
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public Integer visit(FormalParameterList n, MySymbolTable myTable) {
        n.f0.accept(this, myTable);
        n.f1.accept(this, myTable);
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public Integer visit(FormalParameter n, MySymbolTable myTable) {
        argSymbol = new Var();
        UserArgType = true;
        n.f0.accept(this, myTable);
        UserArgType = false;
        argVal = true ;
        n.f1.accept(this, myTable);
        argVal = false;
        myTable.insert(argSymbol.getName(),argSymbol);//vale ston pinaka tis function
        argSymbol = null ;
        return null;
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    public Integer visit(FormalParameterTail n, MySymbolTable myTable) {
        if(n.f0.present())
            n.f0.accept(this, myTable);
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public Integer visit(FormalParameterTerm n, MySymbolTable myTable) {
        n.f1.accept(this, myTable);
        return null;
    }


    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public Integer visit(Type n, MySymbolTable myTable) {
        return n.f0.accept(this, myTable);
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public Integer visit(ArrayType n, MySymbolTable myTable) {
        if(mySymbol!=null)                              //kai gia var kai gia sinartisi
            mySymbol.setReturnType(n.f0.toString()+n.f1.toString()+n.f2.toString());
        else {
            ((Function) myFuncPtr).addArg(n.f0.toString() + n.f1.toString() + n.f2.toString());    //Method's argument
            argSymbol.setReturnType(n.f0.toString()+n.f1.toString()+n.f2.toString());
        }
        return null;
    }

    /**
     * f0 -> "boolean"
     */
    public Integer visit(BooleanType n, MySymbolTable myTable) {
        if(mySymbol!=null)
            mySymbol.setReturnType(n.f0.toString());
        else {
            ((Function) myFuncPtr).addArg(n.f0.toString());                           //Method's argument
            argSymbol.setReturnType(n.f0.toString());
        }
        return null;
    }

    /**
     * f0 -> "int"
     */
    public Integer visit(IntegerType n, MySymbolTable myTable) {
        if(mySymbol!=null)                                      //Field or Method return type
            mySymbol.setReturnType(n.f0.toString());
        else {
            ((Function) myFuncPtr).addArg(n.f0.toString());                           //Method's argument
            argSymbol.setReturnType(n.f0.toString());
        }
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public Integer visit(Identifier n, MySymbolTable myTable) {  //prwti fora exei null return type 2i fora oxi
        if(mySymbol!=null) {
            if (mySymbol.getReturnType() == null)                         //Return type ->Identifier
                mySymbol.setReturnType(n.f0.toString());
            else
                mySymbol.setName(n.f0.toString());                         //vale to onoma tis metavlitis /sinartisis    -identifier
        }
        if(UserArgType) {
            ((Function) myFuncPtr).addArg(n.f0.toString());
             argSymbol.setReturnType(n.f0.toString());
        }
        if(argVal){
            argSymbol.setName(n.f0.toString());
        }
        return null;
    }
}
