package relations;

/**
 * Created by kyriakos on 30/5/2016.
 */
public class ConstMove extends Tuple {
    private Integer i_counter ;
    private String variable ;
    private String constant ;

    public ConstMove(){
        i_counter = 0 ;
        variable = null;
        constant = null ;
    }

    public void setI_counter(Integer i_counter) {
        this.i_counter = i_counter;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setConstant(String constant) {
        this.constant = constant;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("constMove(\"");
        builder.append(method_name);
        builder.append("\", ");
        builder.append(i_counter.toString());
        builder.append(", \"");
        builder.append(variable);
        builder.append("\", ");
        builder.append(constant.toString());
        builder.append(").");
        return builder.toString();
    }
}
