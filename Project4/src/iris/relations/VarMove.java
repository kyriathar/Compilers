package relations;


/**
 * Created by kyriakos on 30/5/2016.
 */
public class VarMove extends Tuple {
    private Integer i_counter ;
    private String variable1 ;
    private String variable2 ;

    public VarMove(){
        i_counter = 0 ;
        variable1 = null;
        variable2 = null ;
    }

    public void setI_counter(Integer i_counter) {
        this.i_counter = i_counter;
    }

    public void setVariable1(String variable1) {
        this.variable1 = variable1;
    }

    public void setVariable2(String variable2) {
        this.variable2 = variable2;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("varMove(\"");
        builder.append(method_name);
        builder.append("\", ");
        builder.append(i_counter.toString());
        builder.append(", \"");
        builder.append(variable1);
        builder.append("\", \"");
        builder.append(variable2);
        builder.append("\").");
        return builder.toString();
    }
}
