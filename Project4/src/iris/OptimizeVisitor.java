import filename.FileName;
import optimstructs.ConstantPropagation;
import optimstructs.DeadCode;
import relations.VarDef;
import relations.VarUse;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kyriakos on 4/6/2016.
 */

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

public class OptimizeVisitor extends GJDepthFirst<String,String> {
    private int curr_i ;
    private SpigletBuffer spigletBuffer ;
    private boolean isMove = false ;

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public String visit(Goal n, String argu) {
        curr_i = 0 ;
        spigletBuffer = new SpigletBuffer();
        spigletBuffer.emit("MAIN");
        n.f1.accept(this, "MAIN");
        spigletBuffer.emit("END");
        if(n.f3.present()){
            n.f3.accept(this, argu);
        }
        try {
            spigletBuffer.bufferToFile();
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
        String procedureName = n.f0.f0.toString();
        StringBuilder builder = new StringBuilder();
        builder.append(procedureName);
        builder.append(" ");
        builder.append(n.f1.toString());
        builder.append(n.f2.accept(this,argu));
        builder.append(n.f3.toString());
        spigletBuffer.emit(builder.toString());
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
        spigletBuffer.emit_T(n.f0.toString());
        return null;
    }

    /**
     * f0 -> "ERROR"
     */
    public String visit(ErrorStmt n, String argu) {
        curr_i ++ ;
        spigletBuffer.emit_T(n.f0.toString());
        return null;
    }


    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public String visit(CJumpStmt n, String argu) {
        curr_i ++ ;
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");
        builder.append(n.f1.accept(this,argu));
        builder.append(" ");
        builder.append(n.f2.f0.toString());
        spigletBuffer.emit_T(builder.toString());
        return null;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public String visit(JumpStmt n, String argu) {
        curr_i ++ ;
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");
        builder.append(n.f1.f0.toString());
        spigletBuffer.emit_T(builder.toString());
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
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");
        builder.append(n.f1.accept(this,argu));
        builder.append(" ");
        builder.append(n.f2.accept(this,argu));
        builder.append(" ");
        builder.append(n.f3.accept(this,argu));
        spigletBuffer.emit_T(builder.toString());
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
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");
        builder.append(n.f1.accept(this,argu));
        builder.append(" ");
        builder.append(n.f2.accept(this,argu));
        builder.append(" ");
        builder.append(n.f3.accept(this,argu));
        spigletBuffer.emit_T(builder.toString());
        return null;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public String visit(MoveStmt n, String argu) {
        curr_i ++ ;
        if(DeadCode.removeInstruction(argu,Integer.toString(curr_i)))
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");
        builder.append(n.f1.accept(this,argu));
        builder.append(" ");
        isMove = true ;
        //builder.append(n.f2.accept(this,argu));
        String temp = n.f2.accept(this,argu);
        String constValue = null ;
        constValue = ConstantPropagation.get(argu,Integer.toString(curr_i),temp);
        if(constValue != null) {
            builder.append(constValue);
        }
        else{
            builder.append(temp);
        }
        isMove = false ;
        spigletBuffer.emit_T(builder.toString());
        //n.f2.accept(this,argu);
        return null;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public String visit(PrintStmt n, String argu) {
        curr_i ++ ;
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");

        String temp = n.f1.accept(this,argu);
        String constValue = null ;
        constValue = ConstantPropagation.get(argu,Integer.toString(curr_i),temp);
        if(constValue != null) {
            builder.append(constValue);
        }
        else{
            builder.append(temp);
        }
        spigletBuffer.emit_T(builder.toString());
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
        spigletBuffer.emit(n.f0.toString());        //BEGIN
        n.f1.accept(this, argu);
        spigletBuffer.emit(n.f2.toString());            //RETURN
        curr_i ++ ;
        spigletBuffer.emit_T(n.f3.accept(this, argu));
        spigletBuffer.emit(n.f4.toString());            //END
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
        builder.append(n.f0.toString());
        builder.append(" ");
        builder.append(n.f1.accept(this,argu));
        builder.append(n.f2.toString());
        builder.append(" ");
        String argR = null;
        int i = 0;
        argR = n.f3.elementAt(i).accept(this, argu);
        if(argR != null)
            builder.append(argR);

        for (i=1; i < n.f3.size(); i++) {
            argR = n.f3.elementAt(i).accept(this, argu);
            builder.append(" ");
            builder.append(argR);
        }
        builder.append(" ");
        builder.append(n.f4.toString());
        return builder.toString();
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public String visit(HAllocate n, String argu) {
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.toString());
        builder.append(" ");
        //builder.append(n.f1.accept(this,argu));
        String temp = n.f1.accept(this,argu);
        String constValue = null ;
        constValue = ConstantPropagation.get(argu,Integer.toString(curr_i),temp);
        if(constValue != null) {
            builder.append(constValue);
        }
        else{
            builder.append(temp);
        }
        return builder.toString();
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public String visit(BinOp n, String argu) {
        StringBuilder builder = new StringBuilder();
        builder.append(n.f0.accept(this,argu));
        builder.append(" ");
        builder.append(n.f1.accept(this,argu));
        builder.append(" ");
        //builder.append(n.f2.accept(this,argu));
        String temp = n.f2.accept(this,argu);
        String constValue = null ;
        constValue = ConstantPropagation.get(argu,Integer.toString(curr_i),temp);
        if(constValue != null) {
            builder.append(constValue);
        }
        else{
            builder.append(temp);
        }
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
        //String tempName = n.f0.accept(this,argu);
        return n.f0.accept(this, argu);
    }


    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public String visit(Temp n, String argu) {
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
        return n.f0.toString();
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Label n, String argu) {
        if(isMove)
            return n.f0.toString(); //return mono gia move
        spigletBuffer.emit(n.f0.toString());
        return null;
    }


}
