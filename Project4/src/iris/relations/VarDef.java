package relations;

/**
 * Created by kyriakos on 31/5/2016.
 */
public class VarDef extends Tuple{
    private Integer i_counter ;
    private String variable ;

    public VarDef(){
        i_counter = 0 ;
        variable = null ;
    }

    public void setI_counter(Integer i_counter) {
        this.i_counter = i_counter;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("varDef(\"");
        builder.append(method_name);
        builder.append("\", ");
        builder.append(i_counter.toString());
        builder.append(", \"");
        builder.append(variable);
        builder.append("\").");
        return builder.toString();
    }
}
