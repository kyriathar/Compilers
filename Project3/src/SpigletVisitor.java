import filename.FileName;
import mapping.Function;
import mapping.MySymbol;
import mapping.MySymbolTable;
import mapping.Var;
import syntaxtree.*;
import visitor.GJDepthFirst;
import vmapping.Class;
import vmapping.ClassContainer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hduser on 6/5/2016.
 */

class RegToVarsMap{
    private Map<String,MySymbol> map ;

    public RegToVarsMap(){
        map = new HashMap<>();
    }

    public void add(String regName , MySymbol var){
        map.put(regName,var);
    }

    public String getRegType(String regName){
        return map.get(regName).getReturnType();
    }
}



class Temps{
    private int counter ;

    public Temps(){
        counter = 0 ;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public String createTemp(){
        StringBuilder builder = new StringBuilder();
        builder.append("TEMP ");
        builder.append(Integer.toString(counter));
        counter++ ;
        return builder.toString();
    }
}

class Labels{
    private int counter ;

    public Labels(){
        counter = 1 ;
    }

    public String createLabel(){
        StringBuilder builder = new StringBuilder();
        builder.append("L");
        builder.append(Integer.toString(counter));
        counter++ ;
        return builder.toString();
    }
}

class SpigletBuffer{
    private List<String> buffer ;

    public SpigletBuffer(){
        buffer = new ArrayList<>();
    }

    public void emit(String label){
        this.buffer.add(label);
    }

    public void emit_T(String instruction){
        this.buffer.add("\t" + instruction);
    }

    public void addLine(){
        this.buffer.add("");
    }

    public void bufferToFile() throws FileNotFoundException, UnsupportedEncodingException {
        String fileNameSpg = FileName.name;
        PrintWriter writer = new PrintWriter(fileNameSpg, "UTF-8");
        for(String instr : buffer){
            writer.println(instr);
        }
        writer.close();
    }

}

public class SpigletVisitor extends GJDepthFirst<String, MySymbolTable> {
    private SpigletBuffer spigletBuffer ;
    private ClassContainer classContainer ;
    private Temps temps ;
    private Labels labels ;
    private RegToVarsMap regToVarsMap ;
    private List<String> methodArgs ;

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, MySymbolTable myTable) {
        String _ret=null;
        spigletBuffer = new SpigletBuffer();
        classContainer = new ClassContainer();
        classContainer.createClasses(myTable);              //exoume ta V tables se Java
        temps = new Temps();
        int maxArgs = myTable.findMaxArguments();
        //System.out.println("maxArgs = "+ maxArgs);
        temps.setCounter(maxArgs + 20 );
        //System.out.println("counter = "+temps.getCounter());

        labels = new Labels();
        regToVarsMap = new RegToVarsMap();
        String mainClassName = n.f0.f1.f0.toString() ;                     //Main class name
        n.f0.accept(this,myTable.enter(mainClassName));                    //enter MainClass - f0 scope
        //get other classes
        if(n.f1.present()){
            n.f1.accept(this,myTable);
        }
        try {
            spigletBuffer.bufferToFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
        spigletBuffer.emit("MAIN");
        //global V tables
        for(Class currClass : classContainer.getClasses()){
            String vTableLabelR = temps.createTemp();
            spigletBuffer.emit_T("MOVE " + vTableLabelR + " " + currClass.getVtable().getName()); //vale sto holder to addrr tou vtable label
            String vTableAllocateR = temps.createTemp();
            spigletBuffer.emit_T("MOVE " + vTableAllocateR + " " + "HALLOCATE " +currClass.getVtable().getSize()); //allocate to Vtable
            spigletBuffer.emit_T("HSTORE " + vTableLabelR + " 0 " + vTableAllocateR ); //vale sto sto vtable label point na deiksei sto vtable
            List<String> fullNameList = currClass.getVtable().getFullNameList();
            for(String methodName : fullNameList){
                String methodLabelR = temps.createTemp();
                spigletBuffer.emit_T("MOVE " + methodLabelR + " " + methodName );
                spigletBuffer.emit_T("HSTORE " + vTableAllocateR + " "+ currClass.getVtable().getMethodOffset(methodName) +" " + methodLabelR );
            }
            spigletBuffer.addLine();
        }
        spigletBuffer.addLine();

        String mainName = "main";
        String wrappedMainName = Function.WrapFunc(mainName);
        MySymbolTable myMainTable =myTable.enter(wrappedMainName);

        if(n.f15.present()){
            n.f15.accept(this,myMainTable);
        }

        spigletBuffer.emit("END");
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, MySymbolTable myTable) {
        n.f0.accept(this, myTable);
        return null ;
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
        //eisai se class table -> classScope exoume tin klasi mas !
        spigletBuffer.addLine();
        String methodName = n.f2.f0.toString();
        String className = myTable.getClassScope();
        StringBuilder builder = new StringBuilder();
        builder.append(className);
        builder.append("_");
        builder.append(methodName);
        int methodArgNo = ((Function)myTable.getSymbol(Function.WrapFunc(methodName))).getArgs().size();
        methodArgNo++;  //add this
        builder.append(" ");
        builder.append("[");
        builder.append(Integer.toString(methodArgNo));
        builder.append("]");
        spigletBuffer.emit(builder.toString());
        spigletBuffer.emit("BEGIN");

        //set arguments to registers
        methodArgs = new ArrayList<>();
        if(n.f4.present()){
            n.f4.accept(this,myTable);
        }
        MySymbolTable myFuncTable =myTable.enter(Function.WrapFunc(methodName));

        int i = 1 ;
        for(String arg : methodArgs){
            String tempR = new String("TEMP "+Integer.toString(i));
            MySymbol var = myFuncTable.getSymbol(arg) ;
            ((Var)var).setRegister(tempR);
            regToVarsMap.add(tempR, var);
            i++ ;
        }

        if(n.f8.present()){     // Statements
            n.f8.accept(this,myFuncTable);              //enter function's scope
        }


        myFuncTable.mySetLeftOrRight("r");
        String ret = n.f10.accept(this, myFuncTable);
        spigletBuffer.emit("RETURN");
        spigletBuffer.emit_T(ret);
        spigletBuffer.emit("END");
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, MySymbolTable myTable) {
        n.f0.accept(this, myTable);
        n.f1.accept(this, myTable);
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, MySymbolTable myTable) {
        String argName = n.f1.f0.toString();
        methodArgs.add(argName);
        return null;
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    public String visit(FormalParameterTail n, MySymbolTable myTable) {
        if(n.f0.present())
            n.f0.accept(this, myTable);
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n, MySymbolTable myTable) {
        n.f1.accept(this, myTable);
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
        n.f0.accept(this, myTable);
        return null;
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
        myTable.mySetLeftOrRight("l");    //STORE code
        String leftR = n.f0.accept(this, myTable);      //exoume to aristero TEMP
        myTable.mySetLeftOrRight("r");    //LOAD code
        String rightR = n.f2.accept(this, myTable);

        String leftVarName = n.f0.f0.toString() ;
        MySymbol islocalVariable = myTable.getSymbol(leftVarName);
        if(islocalVariable != null)
            spigletBuffer.emit_T("MOVE " + leftR + " " + rightR );
        else
            spigletBuffer.emit_T("HSTORE " + leftR + " 0 " + rightR );      //tha prepei na ginei edw to store an den einai local variable....

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
        myTable.mySetLeftOrRight("r");    //LOAD code
        String arrayAddr = n.f0.accept(this, myTable);      //array addr ok
        String arrayAddrR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayAddrR + " " + arrayAddr);
        String arrayIndex = n.f2.accept(this, myTable);                 //array index ok
        String arrayIndexR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayIndexR + " " + arrayIndex);
        String indexLessThanZeroR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ indexLessThanZeroR + " LT " + arrayIndexR + " 0");
        String L5 = labels.createLabel();
        spigletBuffer.emit_T("CJUMP " + indexLessThanZeroR +" "+ L5);
        spigletBuffer.emit_T("ERROR");
        //check if index is in the bounds of the array
        spigletBuffer.emit(L5 +"  NOOP");
        String arraySizeR = temps.createTemp();
        spigletBuffer.emit_T("HLOAD " + arraySizeR +" " +arrayAddrR+" 0");
        String oneR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + oneR + " 1");
        String indexInBoundsR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+indexInBoundsR +" LT " + arrayIndexR +" "+arraySizeR);
        String indexInBoundsNotR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+indexInBoundsNotR+" MINUS " + oneR + " " +indexInBoundsR);
        String L6 = labels.createLabel();
        spigletBuffer.emit_T("CJUMP "+ indexInBoundsNotR +" "+L6);
        spigletBuffer.emit_T("ERROR");
        // STORE
        spigletBuffer.emit(L6 +"  NOOP");
        String arrayIndexBytesR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayIndexBytesR + " TIMES " + arrayIndexR +" 4" );
        String arrayIndexBytesPlusOneR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+arrayIndexBytesPlusOneR + " PLUS " +arrayIndexBytesR + " 4");
        String elementAddrR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+elementAddrR + " PLUS " +arrayAddrR +" " + arrayIndexBytesPlusOneR);   //store here
        String storeElement =  n.f5.accept(this, myTable);
        String storeElementR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+storeElementR +" " +storeElement);
        spigletBuffer.emit_T("HSTORE "+elementAddrR+" 0 "+ storeElementR);
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
        String conditionR = n.f2.accept(this, myTable);         //pare tin timi tou if
        String elseL = labels.createLabel();
        String endL = labels.createLabel();
        spigletBuffer.emit_T("CJUMP " + conditionR + " " + elseL );    //PROSOXI : na ferei register alliws kane allocate new
        n.f4.accept(this, myTable);             //if statements
        spigletBuffer.emit_T("JUMP " + endL );
        spigletBuffer.emit(elseL + "  NOOP");
        n.f6.accept(this, myTable);             //else statements
        spigletBuffer.emit(endL + "  NOOP");
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
        String startL = labels.createLabel();
        String endL = labels.createLabel();
        spigletBuffer.emit(startL + "  NOOP");
        String conditionR = n.f2.accept(this, myTable);
        spigletBuffer.emit_T("CJUMP " + conditionR + " " + endL );    //PROSOXI : na ferei register alliws kane allocate new
        n.f4.accept(this, myTable);
        spigletBuffer.emit_T("JUMP " + startL );
        spigletBuffer.emit(endL + "  NOOP");
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
        String R = n.f2.accept(this, myTable);
        spigletBuffer.emit_T("PRINT " + R );
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
        String E = labels.createLabel();
        String resultR = temps.createTemp();
        String t1R = n.f0.accept(this, myTable);
        spigletBuffer.emit_T("MOVE " + resultR + " " + t1R );
        spigletBuffer.emit_T("CJUMP " + resultR + " " + E );//an einai true sinexizei apo katw
        String t2R = n.f2.accept(this, myTable);
        spigletBuffer.emit_T("MOVE " + resultR + " " + t2R );
        spigletBuffer.emit(E + "  NOOP");
        return resultR;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String leftR = n.f0.accept(this, myTable);
        myTable.mySetLeftOrRight("r");    //LOAD code
        String rightR = n.f2.accept(this, myTable);
        String resultR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + resultR +" LT "+ leftR + " " + rightR);        //an leftR< rightR  tote resultR = 1
        return resultR;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String leftR = n.f0.accept(this, myTable);
        myTable.mySetLeftOrRight("r");    //LOAD code
        String rightR = n.f2.accept(this, myTable);
        String resultR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + resultR +" PLUS "+ leftR + " " + rightR);        //an leftR< rightR  tote resultR = 1
        return resultR;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String leftR = n.f0.accept(this, myTable);
        myTable.mySetLeftOrRight("r");    //LOAD code
        String rightR = n.f2.accept(this, myTable);
        String resultR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + resultR +" MINUS "+ leftR + " " + rightR);        //an leftR< rightR  tote resultR = 1
        return resultR;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String leftR = n.f0.accept(this, myTable);
        myTable.mySetLeftOrRight("r");    //LOAD code
        String rightR = n.f2.accept(this, myTable);
        String resultR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + resultR +" TIMES "+ leftR + " " + rightR);        //an leftR< rightR  tote resultR = 1
        return resultR;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String arrayAddr = n.f0.accept(this, myTable);
        String arrayAddrR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayAddrR + " " + arrayAddr);
        String arrayIndex = n.f2.accept(this, myTable);
        String arrayIndexR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayIndexR + " " + arrayIndex);
        String indexLessThanZeroR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ indexLessThanZeroR + " LT " + arrayIndexR + " 0");
        String L5 = labels.createLabel();
        spigletBuffer.emit_T("CJUMP " + indexLessThanZeroR +" "+ L5);
        spigletBuffer.emit_T("ERROR");

        spigletBuffer.emit(L5 +"  NOOP");
        String arraySizeR = temps.createTemp();
        spigletBuffer.emit_T("HLOAD " + arraySizeR +" " +arrayAddrR+" 0");
        String oneR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + oneR + " 1");
        String indexInBoundsR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+indexInBoundsR +" LT " + arrayIndexR +" "+arraySizeR);
        String indexInBoundsNotR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+indexInBoundsNotR+" MINUS " + oneR + " " +indexInBoundsR);
        String L6 = labels.createLabel();
        spigletBuffer.emit_T("CJUMP "+ indexInBoundsNotR +" "+L6);
        spigletBuffer.emit_T("ERROR");

        spigletBuffer.emit(L6 +"  NOOP");
        String arrayIndexBytesR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayIndexBytesR + " TIMES " + arrayIndexR +" 4" );
        String arrayIndexBytesPlusOneR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+arrayIndexBytesPlusOneR + " PLUS " +arrayIndexBytesR + " 4");
        String elementAddrR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+elementAddrR + " PLUS " +arrayAddrR +" " + arrayIndexBytesPlusOneR);
        String elementR =  temps.createTemp();
        spigletBuffer.emit_T("HLOAD "+elementR+" "+ elementAddrR +" 0");
        return elementR;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String arrayAddr = n.f0.accept(this, myTable);
        String arrayAddrR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+ arrayAddrR + " " + arrayAddr);
        String arrayLengthR = temps.createTemp();
        spigletBuffer.emit_T("HLOAD " + arrayLengthR +" "+ arrayAddrR + " 0" );
        return arrayLengthR;
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
        //pare ton kataxwriti pou exei ti dieuthinsi tou antikeimenou   -->PrimaryExpression

        spigletBuffer.addLine();
        spigletBuffer.addLine();
        spigletBuffer.addLine();
        myTable.mySetmsgSend(true);
        myTable.mySetLeftOrRight("r");    //LOAD code
        String obj = n.f0.accept(this, myTable);
        myTable.mySetmsgSend(false);
        String objR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+objR +" " +obj);       //to address tou antikeimenou !

        //vres se pio antikeimeno anikei gia na pas sto swsto V tabel
        String className = regToVarsMap.getRegType(obj);
        //an epistrepsei null => einai new A().foo periptwsi

        String methodName = n.f2.f0.toString();
        String methodName_copy = new String(methodName);
        //methodName =className+"_"+methodName;
        int methodOffset = classContainer.getClass(className).getVtable().getMethodOffset2(methodName);
        String vTableAddrR = temps.createTemp();
        spigletBuffer.emit_T("HLOAD "+ vTableAddrR +" "+objR + " 0");
        String methodAddrR = temps.createTemp();
        spigletBuffer.emit_T("HLOAD "+ methodAddrR +" "+vTableAddrR + " " + Integer.toString(methodOffset));
        //edw ftiakse ta arguments !
        String args ="";
        if(n.f4.present()){         //check arguments
            args = n.f4.accept(this,myTable);
        }

        String msgSendRetR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " +msgSendRetR + " CALL "+methodAddrR + "( "+ objR+" "+ args +" )" );

        Var var = new Var();    //gia periptwseis opws : auxkey2 = (p_node.GetLeft()).GetKey() ;
        String varClass = myTable.findMethodRetunType(className,methodName_copy);
        var.setReturnType(varClass);   //prepei na valeis auto pou epistrefei i GetKey
        var.setRegister(msgSendRetR);
        regToVarsMap.add(msgSendRetR,var);

        return msgSendRetR;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, MySymbolTable myTable) {
        StringBuilder builder = new StringBuilder();
        myTable.mySetLeftOrRight("r");    //LOAD code
        String firstArgR = n.f0.accept(this, myTable);
        builder.append(firstArgR);
        builder.append(" ");
        String tail = "";
        if(n.f1.f0.present())
            tail = n.f1.accept(this, myTable);
        builder.append(tail);
        return builder.toString();
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, MySymbolTable myTable) {
        StringBuilder tail = new StringBuilder();
        String argR = null;
        int i = 0;
        argR = n.f0.elementAt(i).accept(this, myTable);
        tail.append(argR);
        tail.append(" ");
        for (i=1; i < n.f0.size(); i++) {
            argR = n.f0.elementAt(i).accept(this, myTable);
            tail.append(argR);
            tail.append(" ");
        }
        return tail.toString();
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, MySymbolTable myTable) {
        myTable.mySetLeftOrRight("r");    //LOAD code
        String argR  = n.f1.accept(this, myTable);
        return argR;
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
        String r = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + r +" "+ n.f0.toString());
        return r;
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, MySymbolTable myTable) {
        String r = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + r +" "+ "1");
        return r;
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, MySymbolTable myTable) {
        String r = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + r +" "+ "0");
        return r;
    }


    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, MySymbolTable myTable) {
        //cases ...analogws ti einai to variable
        String tempR =null;
        String varName = n.f0.toString();

        if(myTable.myGetLeftOrRight().equals("l")) {
            // local i data member
            MySymbol var = myTable.getSymbol(varName);
            if(var != null) {     //LOCAL VARIABLE
                if (((Var) var).getRegister() != null) {
                    tempR = ((Var) var).getRegister();
                } else {
                    //prwti fora pou vazoume ena register se mia metavliti
                    tempR = temps.createTemp();
                    ((Var) var).setRegister(tempR);
                    regToVarsMap.add(tempR, var);
                }
            }else{
                var = myTable.findField(varName);
                if(var == null){
                    System.out.println("SpigletVisitor::Identifier :: den yparxei tetio field => semantic error left "+ varName);
                }
                StringBuilder builder = new StringBuilder();
                String className = ((Var)var).getClassScope();
                builder.append(className);
                builder.append("_");
                builder.append(varName);
                String fieldFullName = builder.toString();
                int fieldOffset = classContainer.getClass(className).getFields().getFieldOffset(fieldFullName);
                //epistrofi dieuthinsis pou tha kaneis STORE
                tempR = temps.createTemp();
                spigletBuffer.emit_T("MOVE "+ tempR + " PLUS " +"TEMP 0 " + Integer.toString(fieldOffset));

            }
        }else if (myTable.myGetLeftOrRight().equals("r")){
            // local i data member
            MySymbol var = myTable.getSymbol(varName);
            if(var != null) {     //LOCAL VARIABLE
                if (((Var) var).getRegister() != null) {
                    tempR = ((Var) var).getRegister();
                } else {
                    //prwti fora pou vazoume ena register se mia metavliti
                    tempR = temps.createTemp();
                    ((Var) var).setRegister(tempR);
                    regToVarsMap.add(tempR, var);
                }
            }else{
                var = myTable.findField(varName);
                if(var == null){
                    System.out.println("SpigletVisitor::Identifier :: den yparxei tetio field => semantic error right");
                }
                StringBuilder builder = new StringBuilder();
                String className = ((Var)var).getClassScope();
                builder.append(className);
                builder.append("_");
                builder.append(varName);
                String fieldFullName = builder.toString();
                int fieldOffset = classContainer.getClass(className).getFields().getFieldOffset(fieldFullName);
                //LOAD se kataxwriti kai epistrofi
                tempR = temps.createTemp();
                ((Var) var).setRegister(tempR);
                regToVarsMap.add(tempR, var);
                spigletBuffer.emit_T("HLOAD "+ tempR +" TEMP 0 " +Integer.toString(fieldOffset));
            }

        }

        if(tempR == null)
        {
            System.out.println("IDENTIFIER:: tempR ERROR "+varName);
        }


        return tempR;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, MySymbolTable myTable) {
        String thisR = new String("TEMP 0");
        //myTable -> functions Table -> get classScope
        MySymbolTable funcTable = myTable.getParent();
        Var var = new Var();    //estw  register to dimiourgw xarin omiomorfias
        var.setReturnType(funcTable.getClassScope());
        var.setRegister(thisR);
        regToVarsMap.add(thisR,var);
        return thisR;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, MySymbolTable myTable) {

        String arraySize = n.f3.accept(this, myTable);      //kataxwritis
        String arraySizeR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + arraySizeR + " " + arraySize);
        String checkForNegativeR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + checkForNegativeR + " LT " + arraySizeR + " 0");
        String L2 = labels.createLabel();
        spigletBuffer.emit_T("CJUMP " + checkForNegativeR + " " + L2);
        spigletBuffer.emit_T("ERROR");

        //spigletBuffer.addLine();
        //array allocation
        spigletBuffer.emit(L2 + "  NOOP");
        String arraySizePlusOneR    = temps.createTemp();
        spigletBuffer.emit_T("MOVE " +arraySizePlusOneR + " PLUS " + arraySizeR + " 1");
        String arraySizePlusOneBytesR    = temps.createTemp();
        spigletBuffer.emit_T("MOVE " +arraySizePlusOneBytesR + " TIMES " + arraySizePlusOneR + " 4");
        String arrayAddrR    = temps.createTemp();
        spigletBuffer.emit_T("MOVE " +arrayAddrR + " HALLOCATE " + arraySizePlusOneBytesR);
        //array initialization
        String zeroR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+zeroR+" 0");
        String currOffsetR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " +currOffsetR + " 4");      //ignore array length
        String L3 = labels.createLabel();
        spigletBuffer.emit(L3 + "  NOOP");
        String currLessThanTopR = temps.createTemp();
        spigletBuffer.emit_T("MOVE " + currLessThanTopR +" LT " + currOffsetR + " " + arraySizePlusOneBytesR);
        String L4 = labels.createLabel();
        spigletBuffer.emit_T("CJUMP " + currLessThanTopR + " "+ L4);
        String currArrayOffsetR = temps.createTemp();
        spigletBuffer.emit_T("MOVE "+currArrayOffsetR + " PLUS "+arrayAddrR +" "+currOffsetR);
        spigletBuffer.emit_T("HSTORE "+currArrayOffsetR +" 0 " +zeroR);
        spigletBuffer.emit_T("MOVE " +currOffsetR +" PLUS " +currOffsetR +" 4");
        spigletBuffer.emit_T("JUMP "+L3);
        // set array length
        spigletBuffer.emit(L4 + "  NOOP");
        spigletBuffer.emit_T("HSTORE " + arrayAddrR + " 0 " + arraySizeR);

        spigletBuffer.addLine();
        spigletBuffer.addLine();

        return arrayAddrR;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, MySymbolTable myTable) {
        //na pareis to megethos tou User defined typou
        String className = n.f1.f0.toString() ;
        Class currClass = classContainer.getClass(className) ;
        int objectSize = currClass.size();
        // Όταν κάνουμε new ένα νέο αντικείμενο τύπου Fac, όπως και πριν, αρχικά
        // κάνουμε ένα HALLOCATE για να δεσμεύσουμε τον χώρο στο heap.
        //MOVE TEMP 25 HALLOCATE 4
        String objR = temps.createTemp() ;       //TEMP 25 (TEMP 34)
        spigletBuffer.emit_T("MOVE " + objR +" HALLOCATE " + Integer.toString(objectSize));
        // Τώρα για να πάρουμε την τιμή του vTable αρκεί να φέρουμε το label Fac_vTable σε ένα TEMP
        // και μετά να κάνουμε ένα HLOAD για να πάρουμε την διεύθυνση του vTable.
        //MOVE TEMP 26 Fac_vTable
        String vTableLabelR = temps.createTemp();    //TEMP 26
        spigletBuffer.emit_T("MOVE " + vTableLabelR +" " + currClass.getVtable().getName());
        // Μετά το τέλος αυτής της εντολής το TEMP 27 έχει τη διεύθυνση του vTable.
        //HLOAD TEMP 27 TEMP 26 0
        String vTableR = temps.createTemp(); //TEMP 27
        spigletBuffer.emit_T("HLOAD " + vTableR +" " + vTableLabelR + " " +"0");

        spigletBuffer.addLine();
        //arxikopiise ola ta pedia se 0 ektos tou 1ou (V_table adress)
        //MOVE TEMP 35 4
        String currElementR = temps.createTemp();     //TEMP 35
        spigletBuffer.emit_T("MOVE " + currElementR + " " + "4");
        //L0     NOOP
        String L0 = labels.createLabel();       //L0
        spigletBuffer.emit(L0);
        spigletBuffer.emit_T("NOOP");
        //MOVE TEMP 183 12
        String objSizeR = temps.createTemp();       //TEMP 183
        spigletBuffer.emit_T("MOVE " + objSizeR + " " + Integer.toString(objectSize));
        //MOVE TEMP 184 LT TEMP 35 TEMP 183
        String inObjR = temps.createTemp();     //TEMP 184
        spigletBuffer.emit_T("MOVE " + inObjR + " LT "+ currElementR + " " + objSizeR);
        //CJUMP TEMP 184 L1
        String L1 = labels.createLabel();       //L1
        spigletBuffer.emit_T("CJUMP " + inObjR + " " + L1);
        //MOVE TEMP 185 PLUS TEMP 34 TEMP 35
        String nextElementR = temps.createTemp();     //TEMP 185
        spigletBuffer.emit_T("MOVE " + nextElementR +" PLUS " + objR + " " + currElementR);
        //MOVE TEMP 186 0
        String zeroR = temps.createTemp();     //TEMP 186
        spigletBuffer.emit_T("MOVE " + zeroR +" 0");
        //HSTORE TEMP 185 0 TEMP 186
        spigletBuffer.emit_T("HSTORE " + nextElementR +" 0 " + zeroR);
        //MOVE TEMP 35 PLUS TEMP 35 4
        spigletBuffer.emit_T("MOVE " + currElementR +" PLUS "+ currElementR +" " +"4");
        //JUMP L0
        spigletBuffer.emit_T("JUMP " + L0 );
        //L1     NOOP
        spigletBuffer.emit(L1);
        spigletBuffer.emit_T("NOOP");

        spigletBuffer.addLine();
        // Και εδώ είναι το HSTORE που αποθηκεύει τη διεύθυνση του vTable στην αρχή του αντικειμένου.
        //HSTORE TEMP 25 0 TEMP 27
        spigletBuffer.emit_T("HSTORE " + objR +" 0 " + vTableR);
        //epistrefeis ti dieuthinsi tou antikeimenou
        //krate record se periptwsi opws new A().foo();
        if(myTable.myGetMsgSend()==true){
            Var var = new Var();    //estw  register to dimiourgw xarin omiomorfias
            var.setReturnType(n.f1.f0.toString());
            var.setRegister(objR);
            regToVarsMap.add(objR,var);
        }
        return objR;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, MySymbolTable myTable) {
        String oneR = temps.createTemp();
        String resultR = temps.createTemp();
        String clauseR = n.f1.accept(this, myTable);
        spigletBuffer.emit_T("MOVE " + oneR +" 1");
        spigletBuffer.emit_T("MOVE " + resultR +" MINUS "+ oneR + " " + clauseR);        //afairesi apo to 1 dinei to simplirwma
        return resultR;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, MySymbolTable myTable) {
        String R = n.f1.accept(this, myTable);      //PROSOXI : upotithetai oti panta gurnaei Register apo ta fila !
        return R;
    }
}
