package relations;


public class Instruction extends Tuple{
    private Integer i_counter ;
    private String instruction ;

    public Instruction(){
        i_counter = 0 ;
        instruction = null ;
    }

    public void reset_i_counter() {
        this.i_counter = 0;
    }

    public void setInstruction(String instruction) {
        StringBuilder builder =  new StringBuilder();
        builder.append(instruction);
        this.instruction = builder.toString();
        i_counter ++ ;
    }

    public Integer getI_counter() {
        return i_counter;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("instruction(\"");
        builder.append(method_name);
        if(i_counter < 10)
            builder.append("\",  ");
        else
            builder.append("\", ");
        builder.append(i_counter.toString());
        builder.append(", \"");
        builder.append(instruction);
        builder.append("\").");
        return builder.toString();
    }
}
