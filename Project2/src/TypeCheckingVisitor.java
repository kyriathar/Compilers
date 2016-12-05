import error.LineOfError;
import mapping.Function;
import mapping.MySymbol;
import mapping.MySymbolTable;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by hduser on 11/4/2016.
 */
public class TypeCheckingVisitor extends GJDepthFirst<String, MySymbolTable> {
    MySymbol mySymbol ;
    boolean methodOn = false ;
    Function funcPtr =null;
    List<String> currArgs =null;
    Stack<Function> funStack = new Stack<>();
    Stack<List<String>> argStack = new Stack<>();

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, MySymbolTable myTable) {
        String _ret=null;
        String symbolName = n.f0.f1.f0.toString() ;                     //Main class name
        n.f0.accept(this,myTable.enter(symbolName));                    //enter MainClass - f0 scope
        if(n.f1.present()){
            n.f1.accept(this,myTable);
        }
        return _ret;
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
    public String visit(MainClass n, MySymbolTable myTable) {
        String funcName = "main";
        String wrappedFuncName = Function.WrapFunc(funcName);
        MySymbolTable myFuncTable =myTable.enter(wrappedFuncName);

       if(n.f15.present()){
            n.f15.accept(this,myFuncTable);
        }
        return null;
    }


    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, MySymbolTable myTable) {
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
    public String visit(ClassDeclaration n, MySymbolTable myTable) {
        String symbolName = n.f1.f0.toString() ;                //class name
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
    public String visit(ClassExtendsDeclaration n, MySymbolTable myTable) {
        String symbolName = n.f1.f0.toString() ;                //class name
        if(n.f6.present()){                                     //Methods
            n.f6.accept(this,myTable.enter(symbolName));        //enter Class - f1 scope
        }
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
    public String visit(MethodDeclaration n, MySymbolTable myTable) {
        String funcName = n.f2.f0.toString();
        String wrappedFuncName = Function.WrapFunc(funcName);
        Function currFunction = (Function) myTable.getSymbol(wrappedFuncName);
        MySymbolTable myFuncTable =myTable.enter(wrappedFuncName);
        if(n.f8.present()) {      //exoume statement
            n.f8.accept(this,myFuncTable);
        }
        String returnType = n.f10.accept(this,myFuncTable);
        if(!returnType.equals(currFunction.getReturnType())){
            if(!myTable.SubTypeOf(returnType,currFunction.getReturnType())) {        //Subtype
                System.out.println("TypeCheckingVisitor.MethodDeclaration::Return type does not match Method's return type" + " (line " + Integer.toString(LineOfError.value)+")");
                throw new RuntimeException();
            }
        }
        return null;
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, MySymbolTable myTable) {
        return n.f0.accept(this, myTable);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, MySymbolTable myTable) {
        if(n.f1.present()){
            n.f1.accept(this,myTable);
        }
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, MySymbolTable myTable) {
        String idType=null;
        String expType=null;

        String idName = n.f0.f0.toString();         //get identifier name
        LineOfError.value = n.f0.f0.beginLine ;

        idType = myTable.lookUp(idName);            //find identifier's type
        if(idType ==null){
            System.out.println("TypeCheckingVisitor.AssignmentStatement::Type does not exist" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        expType = n.f2.accept(this, myTable);
        if(expType ==null){
            System.out.println("TypeCheckingVisitor.AssignmentStatement::invalid value" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        if( !idType.equals(expType) ){
            if(!myTable.SubTypeOf(expType,idType)) {        //Subtype
                System.out.println("TypeCheckingVisitor.AssignmentStatement::Wrong Assignment" + " (line " + Integer.toString(LineOfError.value)+")");
                throw new RuntimeException();
            }
        }
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, MySymbolTable myTable) {
        String arrayNameReturn=null;
        String arrayIndexReturn = null;
        String expToAssignReturn = null ;

        arrayNameReturn = n.f0.accept(this, myTable);
        if(!arrayNameReturn.equals("int[]")) {
            System.out.println("TypeCheckingVisitor.ArrayAssignmentStatement::array name not valid" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        arrayIndexReturn = n.f2.accept(this, myTable);
        if(!arrayIndexReturn.equals("int")) {
            System.out.println("TypeCheckingVisitor.ArrayAssignmentStatement::array index not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        expToAssignReturn = n.f5.accept(this, myTable);
        if(!expToAssignReturn.equals("int")) {
            System.out.println("TypeCheckingVisitor.ArrayAssignmentStatement::expression to be assigned not valid" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, MySymbolTable myTable) {
        String expReturn ;

        expReturn = n.f2.accept(this,myTable);
        if(!expReturn.equals("boolean")) {
            System.out.println("TypeCheckingVisitor.IfStatement::expression not boolean" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        n.f4.accept(this, myTable);
        n.f6.accept(this, myTable);
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, MySymbolTable myTable) {
        String expReturn ;
        expReturn = n.f2.accept(this,myTable);
        if(!expReturn.equals("boolean")) {
            System.out.println("TypeCheckingVisitor.WhileStatement::expression not boolean" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        n.f4.accept(this, myTable);
        return null;
    }



    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, MySymbolTable myTable) {
        String expReturn ;
        expReturn = n.f2.accept(this,myTable);
        if(!expReturn.equals("int")) {
            System.out.println("TypeCheckingVisitor.PrintStatement::expression not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return null;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, MySymbolTable myTable) {
        return n.f0.accept(this, myTable);
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, MySymbolTable myTable) {
        String firstReturn =null;
        String secondReturn =null;

        firstReturn = n.f0.accept(this, myTable);
        if(!firstReturn.equals("boolean")){
            System.out.println("TypeCheckingVisitor.AndExpression::first operand is not boolean" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        secondReturn = n.f2.accept(this, myTable);
        if(!secondReturn.equals("boolean")){
            System.out.println("TypeCheckingVisitor.AndExpression::second operand is not boolean" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, MySymbolTable myTable) {
        String firstReturn =null;
        String secondReturn =null;

        firstReturn = n.f0.accept(this, myTable);
        if(!firstReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.CompareExpression::first operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        secondReturn = n.f2.accept(this, myTable);
        if(!secondReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.CompareExpression::second operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, MySymbolTable myTable) {
        String firstReturn =null;
        String secondReturn =null;

        firstReturn = n.f0.accept(this, myTable);
        if(!firstReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.PlusExpression::first operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        secondReturn = n.f2.accept(this, myTable);
        if(!secondReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.PlusExpression::second operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, MySymbolTable myTable) {
        String firstReturn =null;
        String secondReturn =null;

        firstReturn = n.f0.accept(this, myTable);
        if(!firstReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.MinusExpression::first operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        secondReturn = n.f2.accept(this, myTable);
        if(!secondReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.MinusExpression::second operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, MySymbolTable myTable) {
        String firstReturn =null;
        String secondReturn =null;

        firstReturn = n.f0.accept(this, myTable);
        if(!firstReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.TimesExpression::first operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        secondReturn = n.f2.accept(this, myTable);
        if(!secondReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.TimesExpression::second operand is not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, MySymbolTable myTable) {
        String arrayNameReturn=null;
        String arrayIndexReturn = null;

        arrayNameReturn = n.f0.accept(this, myTable);
        if(!arrayNameReturn.equals("int[]")) {
            System.out.println("TypeCheckingVisitor.ArrayLookup::array name not valid" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        arrayIndexReturn = n.f2.accept(this, myTable);
        if(!arrayIndexReturn.equals("int")) {
            System.out.println("TypeCheckingVisitor.ArrayLookup::array index not integer" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, MySymbolTable myTable) {
        String arrayNameReturn=null;

        arrayNameReturn = n.f0.accept(this, myTable);
        if(!arrayNameReturn.equals("int[]")) {
            System.out.println("TypeCheckingVisitor.ArrayLength::array name not valid" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, MySymbolTable myTable) {
        String funcReturn=null;
        String className = null ;
        String funcName =null;

        className = n.f0.accept(this,myTable);
        if(className==null){
            System.out.println("TypeCheckingVisitor.MessageSend::Method does not exist in some class" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }

        funcName = n.f2.f0.toString();
        if(this.funcPtr!=null) {      //deixnei se sinartisi idi
            funStack.push(this.funcPtr);
        }
        LineOfError.value = n.f2.f0.beginLine ;
        this.funcPtr = (Function) myTable.getFunction(funcName,className);      //exoume vrei ti sinartisi pame na doume arguments

        if(currArgs!=null){
            argStack.push(currArgs);
        }
        currArgs = new ArrayList<>();
        if(n.f4.present()){         //check arguments
            n.f4.accept(this,myTable);
        }
        //if(!currArgs.equals(this.funcPtr.getArgs())){
        if(!myTable.check_arguments_equality(currArgs,this.funcPtr.getArgs()))  {
            System.out.println("TypeCheckingVisitor.MessageSend::MessageSend has incorrect arguments" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        if(!argStack.empty()){
            currArgs = argStack.pop() ;
        }else{
            currArgs = null ;
        }
        funcReturn = funcPtr.getReturnType();
        if(!funStack.empty()){
            this.funcPtr = funStack.pop();
        }else {
            this.funcPtr = null;
        }
        return funcReturn;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, MySymbolTable myTable) {
        currArgs.add(n.f0.accept(this, myTable));
        n.f1.accept(this, myTable);
        return null;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, MySymbolTable myTable) {
        if(n.f0.present()){
            n.f0.accept(this, myTable);
        }
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, MySymbolTable myTable) {
        currArgs.add(n.f1.accept(this, myTable));
        return null;
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, MySymbolTable myTable) {
        return n.f0.accept(this, myTable);
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, MySymbolTable myTable) {
        return n.f0.accept(this, myTable);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, MySymbolTable myTable) {
        LineOfError.value = n.f0.beginLine ;
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, MySymbolTable myTable) {
        LineOfError.value = n.f0.beginLine ;
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, MySymbolTable myTable) {
        LineOfError.value = n.f0.beginLine ;
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, MySymbolTable myTable) {
        String varOrFuncName = n.f0.toString() ;
        LineOfError.value = n.f0.beginLine ;
        if(methodOn){
            varOrFuncName = Function.WrapFunc(varOrFuncName);
        }
        String idType = myTable.lookUp(varOrFuncName);
        if(idType == null){
            System.out.println("TypeCheckingVisitor.Identifier::unknown type" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return idType;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, MySymbolTable myTable) {
        LineOfError.value = n.f0.beginLine ;
        return myTable.getThisScope();
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, MySymbolTable myTable) {
        String expReturn =n.f3.accept(this, myTable);
        if(expReturn==null || !expReturn.equals("int")){
            System.out.println("TypeCheckingVisitor.ArrayAllocationExpression::Inbetween brackets NOT integer type" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, MySymbolTable myTable) {
        String idName=n.f1.f0.toString();               //must check that the User defined type exists - not for primitives
        boolean userTypeExists = myTable.UserDefinedTypeExists(idName);
        if(userTypeExists){
            return idName;
        }else{
            System.out.println("TypeCheckingVisitor.AllocationExpression::Type does not exist" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, MySymbolTable myTable) {
        String clauseReturn=null;
        clauseReturn = n.f1.accept(this, myTable);
        if(!clauseReturn.equals("boolean")) {
            System.out.println("TypeCheckingVisitor.NotExpression::clause not boolean" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        return "boolean";
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, MySymbolTable myTable) {
        return n.f1.accept(this, myTable);
    }

}
