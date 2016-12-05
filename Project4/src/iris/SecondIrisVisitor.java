//import com.sun.xml.internal.ws.util.StringUtils;
import nexthelper.IndexLabels;
import relations.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by kyriakos on 30/5/2016.
 */
public class SecondIrisVisitor extends GJDepthFirst<String,String> {
    //next
    private IrisFile nextFile ;
    private Next next ;
    private int curr_i ;
    //varMove
    private IrisFile varMoveFile ;
    private VarMove varMove ;
    private boolean varFlag = false ;
    //constMove
    private IrisFile constMoveFile ;
    private ConstMove constMove ;
    private boolean constFlag = false ;


    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public String visit(Goal n, String argu) {
        //next
        nextFile = new IrisFile();
        nextFile.setName(argu +"/next.iris");
        next = new Next();
        curr_i = 0 ;
        //varMove
        varMoveFile = new IrisFile();
        varMoveFile.setName(argu +"/varMove.iris");
        varMove = new VarMove();
        //constMove
        constMoveFile = new IrisFile();
        constMoveFile.setName(argu +"/constMove.iris");
        constMove = new ConstMove();

        n.f1.accept(this, "MAIN");
        if(n.f3.present()){
            n.f3.accept(this, argu);
        }
        try {
            nextFile.create();
            varMoveFile.create();
            constMoveFile.create();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * f0 -> ( ( Label() )? Stmt() )*
     */
    public String visit(StmtList n, String argu) {
        if(n.f0.present()){
            n.f0.accept(this,argu);
        }
        return null;
    }

    /**
     * f0 -> Label()
     * f1 -> "["
     * f2 -> IntegerLiteral()
     * f3 -> "]"
     * f4 -> StmtExp()
     */
    public String visit(Procedure n, String argu) {
        String procedureName = n.f0.accept(this, argu);
        curr_i = 0 ;
        n.f4.accept(this, procedureName);
        return null;
    }

    /**
     * f0 -> NoOpStmt()
     *       | ErrorStmt()
     *       | CJumpStmt()
     *       | JumpStmt()
     *       | HStoreStmt()
     *       | HLoadStmt()
     *       | MoveStmt()
     *       | PrintStmt()
     */
    public String visit(Stmt n, String argu) {
        n.f0.accept(this, argu);
        return null ;
    }

    /**
     * f0 -> "NOOP"
     */
    public String visit(NoOpStmt n, String argu) {      //OK
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "ERROR"
     */
    public String visit(ErrorStmt n, String argu) {     //OK
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public String visit(CJumpStmt n, String argu) {
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))          //MALLON PREPEI NA FUGEI TELIWS
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        String labelName = n.f2.accept(this, argu) ;
        int jump_next = IndexLabels.getIcounter(argu , labelName);
        next.setJ_counter(jump_next);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public String visit(JumpStmt n, String argu) {      //OK
        curr_i ++ ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        String labelName = n.f1.accept(this, argu) ;
        int jump_next = IndexLabels.getIcounter(argu , labelName);
        next.setJ_counter(jump_next);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Temp()
     * f2 -> IntegerLiteral()
     * f3 -> Temp()
     */
    public String visit(HStoreStmt n, String argu) {        //OK
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Temp()
     * f3 -> IntegerLiteral()
     */
    public String visit(HLoadStmt n, String argu) {         //OK
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public String visit(MoveStmt n, String argu) {          //OK
        curr_i ++ ;
        if(curr_i != IndexLabels.last_instr_map.get(argu)) {
            next.setMethod_name(argu);
            next.setI_counter(curr_i);
            next.setJ_counter(curr_i + 1);
            nextFile.emit(next.toString());
        }
        //varMove - constMove

        String tempName = n.f1.accept(this,argu);

        varFlag = false ;
        constFlag = false ;
        String varOrInt = n.f2.accept(this,argu);

        if(varFlag){
            varMove.setMethod_name(argu);
            varMove.setI_counter(curr_i);
            varMove.setVariable1(tempName);
            varMove.setVariable2(varOrInt);
            varMoveFile.emit(varMove.toString());
            varFlag = false ;
        }

        if(constFlag){
            constMove.setMethod_name(argu);
            constMove.setI_counter(curr_i);
            constMove.setVariable(tempName);
            if(IndexLabels.isNumeric(varOrInt))//an einai numeric min kaneis tpt
            {}
            else {
                StringBuilder builder = new StringBuilder();
                builder.append("\"");
                builder.append(varOrInt);
                builder.append("\"");
                varOrInt = builder.toString();
            } //an einai label vale " "

            constMove.setConstant(varOrInt);
            constMoveFile.emit(constMove.toString());
            constFlag = false ;
        }

        return null;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public String visit(PrintStmt n, String argu) {     //OK
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> Call()
     *       | HAllocate()
     *       | BinOp()
     *       | SimpleExp()
     */
    public String visit(Exp n, String argu) {
        return n.f0.accept(this, argu);
    }


    /**
     * f0 -> "BEGIN"
     * f1 -> StmtList()
     * f2 -> "RETURN"
     * f3 -> SimpleExp()
     * f4 -> "END"
     */
    public String visit(StmtExp n, String argu) {
        n.f1.accept(this, argu);
        curr_i ++ ;
        if(curr_i == IndexLabels.last_instr_map.get(argu))
            return null ;
        next.setMethod_name(argu);
        next.setI_counter(curr_i);
        next.setJ_counter(curr_i + 1);
        nextFile.emit(next.toString());
        return null;
    }

    /**
     * f0 -> "CALL"
     * f1 -> SimpleExp()
     * f2 -> "("
     * f3 -> ( Temp() )*
     * f4 -> ")"
     */
    public String visit(Call n, String argu) {
        String _ret=null;
        return _ret;
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public String visit(HAllocate n, String argu) {
        String _ret=null;
        return _ret;
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public String visit(BinOp n, String argu) {
        String _ret=null;
        return _ret;
    }

    /**
     * f0 -> Temp()
     *       | IntegerLiteral()
     *       | Label()
     */
    public String visit(SimpleExp n, String argu) {
        String _ret = null ;
        _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public String visit(Temp n, String argu) {
        varFlag = true ;
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0);
        builder.append(" ");
        builder.append(n.f1.f0.toString());
        return builder.toString();
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) {
        constFlag = true ;
        return n.f0.toString();
    }


    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Label n, String argu) {
        constFlag = true ;
        return n.f0.toString();
    }
}
