package relations;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyriakos on 30/5/2016.
 */
public class Var extends Tuple {
    private String variable ;
    private Map<String,Integer> map ;

    public Var(){
        variable = null ;
        map = new HashMap<>();
    }

    public void setVariable(String variable) {
        this.variable = variable;
        map.put(variable,1);
    }

    public boolean varExists(String variable){
        Integer i = null ;
        i = map.get(variable);
        if(i != null)
            return true ;
        else
            return false ;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("var(\"");
        builder.append(method_name);
        builder.append("\", \"");
        builder.append(variable);
        builder.append("\").");
        return builder.toString();
    }
}
