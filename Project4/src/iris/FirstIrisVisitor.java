import nexthelper.IndexLabels;
import relations.Instruction;
import relations.Tuple;
import relations.Var;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kyriakos on 30/5/2016.
 */

public class FirstIrisVisitor extends GJDepthFirst<String,String> {
    //instruction
    private IrisFile instrFile ;
    private Instruction instruction ;
    private String noOpLabel = null;
    //var
    private IrisFile varFile ;
    private Var var ;


    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public String visit(Goal n, String argu) {
        //instruction
        instrFile = new IrisFile();
        instrFile.setName(argu +"/instruction.iris");
        instruction = new Instruction();
        //var
        varFile = new IrisFile();
        varFile.setName(argu +"/var.iris");
        var = new Var();
        //nexthelper
        IndexLabels.map = new HashMap<>();
        IndexLabels.last_instr_map = new HashMap<>();

        n.f1.accept(this, "MAIN");
        IndexLabels.last_instr_map.put("MAIN",instruction.getI_counter());
        if(n.f3.present()){
            n.f3.accept(this, argu);
        }
        try {
            instrFile.create();
            varFile.create();
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
        noOpLabel = null ;
        instruction.reset_i_counter();
        n.f4.accept(this, procedureName);
        IndexLabels.last_instr_map.put(procedureName,instruction.getI_counter());
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
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //NOOP
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }

    /**
     * f0 -> "ERROR"
     */
    public String visit(ErrorStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //ERROR
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }       //FTIAXTINA

    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public String visit(CJumpStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //CJUMP
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        builder.append(" ");
        builder.append(n.f2.accept(this, argu));
        noOpLabel = null ;
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public String visit(JumpStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //JUMP
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        noOpLabel = null ;
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Temp()
     * f2 -> IntegerLiteral()
     * f3 -> Temp()
     */
    public String visit(HStoreStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //HSTORE
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        builder.append(" ");
        builder.append(n.f2.accept(this, argu));
        builder.append(" ");
        builder.append(n.f3.accept(this, argu));
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }

    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Temp()
     * f3 -> IntegerLiteral()
     */
    public String visit(HLoadStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //HLOAD
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        builder.append(" ");
        builder.append(n.f2.accept(this, argu));
        builder.append(" ");
        builder.append(n.f3.accept(this, argu));
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public String visit(MoveStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);           //MOVE
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        builder.append(" ");
        builder.append(n.f2.accept(this, argu));
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
        return null;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public String visit(PrintStmt n, String argu) {
        String labelName = null ;
        StringBuilder builder = new StringBuilder();
        if(noOpLabel != null){
            builder.append(noOpLabel);
            builder.append("     ");
            //nexthelper
            labelName = noOpLabel;
            noOpLabel = null ;
        }
        builder.append(n.f0);
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        if(labelName!=null)
            IndexLabels.put(argu,labelName,instruction.getI_counter());
        instrFile.emit(instruction.toString());
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
        StringBuilder builder = new StringBuilder();
        builder.append(n.f2);
        builder.append(" ");
        builder.append(n.f3.accept(this, argu));
        instruction.setMethod_name(argu);
        instruction.setInstruction(builder.toString());
        instrFile.emit(instruction.toString());
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
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0);           //CALL
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        builder.append(n.f2);
        builder.append(" ");
        String argR = null;
        int i = 0;
        argR = n.f3.elementAt(i).accept(this, argu);
        builder.append(argR);
        builder.append(" ");
        for (i=1; i < n.f3.size(); i++) {
            argR = n.f3.elementAt(i).accept(this, argu);
            builder.append(argR);
            builder.append(" ");
        }
        builder.append(n.f4);
        return builder.toString();
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public String visit(HAllocate n, String argu) {
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0);
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        return builder.toString();
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public String visit(BinOp n, String argu) {
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.accept(this, argu));           //CALL
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));
        builder.append(" ");
        builder.append(n.f2.accept(this, argu));
        return builder.toString();
    }

    /**
     * f0 -> "LT"
     *       | "PLUS"
     *       | "MINUS"
     *       | "TIMES"
     */
    public String visit(Operator n, String argu) {
        return n.f0.choice.toString();
    }


    /**
     * f0 -> Temp()
     *       | IntegerLiteral()
     *       | Label()
     */
    public String visit(SimpleExp n, String argu) {
        String _ret = null ;
        _ret = n.f0.accept(this, argu);
        noOpLabel = null ;
        return _ret;
    }

    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public String visit(Temp n, String argu) {
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0);
        builder.append(" ");
        builder.append(n.f1.accept(this, argu));

        //var
        String tempName = builder.toString();
        if(!var.varExists(tempName)){
            var.setMethod_name(argu);
            var.setVariable(tempName);
            varFile.emit(var.toString());
        }

        return builder.toString();
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) {
        return n.f0.toString();
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Label n, String argu) {
        noOpLabel = n.f0.toString();
        return n.f0.toString();
    }

}
