package relations;

/**
 * Created by kyriakos on 30/5/2016.
 */
public class Next extends Tuple{
    private Integer i_counter ;
    private Integer j_counter ;

    public Next(){
        this.i_counter = 0 ;
        this.j_counter = 0 ;
    }

    public void setI_counter(Integer i_counter) {
        this.i_counter = i_counter;
    }

    public void setJ_counter(Integer j_counter) {
        this.j_counter = j_counter;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("next(\"");
        builder.append(method_name);
        if(i_counter < 10)
            builder.append("\",  ");
        else
            builder.append("\", ");
        builder.append(i_counter.toString());
        builder.append(",");
        if(j_counter < 10)
            builder.append("  ");
        else
            builder.append(" ");
        builder.append(j_counter.toString());
        builder.append(").");
        return builder.toString();
    }

}
