import nexthelper.IndexLabels;
import relations.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by kyriakos on 31/5/2016.
 */
public class ThirdIrisVisitor extends GJDepthFirst<String,String> {
    private int curr_i ;
    private boolean tempFlag = false ;
    //varUse
    private IrisFile varUseFile ;
    private VarUse varUse;
    //varDef
    private IrisFile varDefFile ;
    private VarDef varDef;

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public String visit(Goal n, String argu) {
        curr_i = 0 ;
        //varUse
        varUseFile = new IrisFile();
        varUseFile.setName(argu +"/varUse.iris");
        varUse = new VarUse();
        //varDef
        varDefFile = new IrisFile();
        varDefFile.setName(argu +"/varDef.iris");
        varDef = new VarDef();


        n.f1.accept(this, "MAIN");
        if(n.f3.present()){
            n.f3.accept(this, argu);
        }
        try {
            varUseFile.create();
            varDefFile.create();
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
    public String visit(NoOpStmt n, String argu) {
        curr_i ++ ;
        return null;
    }

    /**
     * f0 -> "ERROR"
     */
    public String visit(ErrorStmt n, String argu) {
        curr_i ++ ;
        return null;
    }

    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public String visit(CJumpStmt n, String argu) {
        curr_i ++ ;
        varUse.setMethod_name(argu);
        varUse.setI_counter(curr_i);
        varUse.setVariable(n.f1.accept(this,argu));
        varUseFile.emit(varUse.toString());
        return null;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public String visit(JumpStmt n, String argu) {
        curr_i ++ ;
        return null;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Temp()
     * f2 -> IntegerLiteral()
     * f3 -> Temp()
     */
    public String visit(HStoreStmt n, String argu) {
        curr_i ++ ;
        varUse.setMethod_name(argu);
        varUse.setI_counter(curr_i);
        varUse.setVariable(n.f1.accept(this,argu));
        varUseFile.emit(varUse.toString());

        varUse.setVariable(n.f3.accept(this,argu));
        varUseFile.emit(varUse.toString());
        return null;
    }

    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Temp()
     * f3 -> IntegerLiteral()
     */
    public String visit(HLoadStmt n, String argu) {
        curr_i ++ ;
        varDef.setMethod_name(argu);
        varDef.setI_counter(curr_i);
        varDef.setVariable(n.f1.accept(this,argu));
        varDefFile.emit(varDef.toString());

        varUse.setMethod_name(argu);
        varUse.setI_counter(curr_i);
        varUse.setVariable(n.f2.accept(this,argu));
        varUseFile.emit(varUse.toString());

        return null;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public String visit(MoveStmt n, String argu) {
        curr_i ++ ;
        varDef.setMethod_name(argu);
        varDef.setI_counter(curr_i);
        varDef.setVariable(n.f1.accept(this,argu));
        varDefFile.emit(varDef.toString());

        n.f2.accept(this,argu);
        return null;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public String visit(PrintStmt n, String argu) {
        curr_i ++ ;
       // tempFlag = false ;
        String tempName = n.f1.accept(this,argu);
        /*if(tempFlag){
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(tempName);
            varUseFile.emit(varUse.toString());
            tempFlag = false ;
        }*/
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
        n.f3.accept(this, argu);
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
        //tempFlag = false ;
        String tempName = n.f1.accept(this,argu);
        /*if(tempFlag){
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(tempName);
            varUseFile.emit(varUse.toString());
            tempFlag = false ;
        }*/
        String argR = null;
        int i = 0;
        argR = n.f3.elementAt(i).accept(this, argu);
        if(argR != null){
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(argR);
            varUseFile.emit(varUse.toString());
        }

        for (i=1; i < n.f3.size(); i++) {
            argR = n.f3.elementAt(i).accept(this, argu);
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(argR);
            varUseFile.emit(varUse.toString());
        }

        return _ret;
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public String visit(HAllocate n, String argu) {
        String _ret=null;
        //tempFlag = false ;
        String tempName = n.f1.accept(this,argu);
        /*if(tempFlag){
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(tempName);
            varUseFile.emit(varUse.toString());
            tempFlag = false ;
        }*/
        return _ret;
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public String visit(BinOp n, String argu) {
        varUse.setMethod_name(argu);
        varUse.setI_counter(curr_i);
        varUse.setVariable(n.f1.accept(this,argu));
        varUseFile.emit(varUse.toString());

        //tempFlag = false ;
        String tempName = n.f2.accept(this,argu);
        /*if(tempFlag){
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(tempName);
            varUseFile.emit(varUse.toString());
            tempFlag = false ;
        }*/

        return null;
    }

    /**
     * f0 -> Temp()
     *       | IntegerLiteral()
     *       | Label()
     */
    public String visit(SimpleExp n, String argu) {
        String _ret = null ;
        //_ret = n.f0.accept(this, argu);
        tempFlag = false ;
        String tempName = n.f0.accept(this,argu);
        if(tempFlag){
            varUse.setMethod_name(argu);
            varUse.setI_counter(curr_i);
            varUse.setVariable(tempName);
            varUseFile.emit(varUse.toString());
            tempFlag = false ;
        }

        return _ret;
    }


    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public String visit(Temp n, String argu) {
        tempFlag = true ;
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0);
        builder.append(" ");
        builder.append(n.f1.f0.toString());
        return builder.toString();
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Label n, String argu) {
        return n.f0.toString();
    }
}
