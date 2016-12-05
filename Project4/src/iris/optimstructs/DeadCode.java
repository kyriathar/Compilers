package optimstructs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyriakos on 4/6/2016.
 */
public class DeadCode {
    public static Map<String,Integer> map ;

    public static void add(String funcName ,String i_counter){
        StringBuilder builder = new StringBuilder();
        builder.append(funcName);
        builder.append(i_counter);
        map.put(builder.toString(),new Integer(1));
    }

    public static boolean removeInstruction(String funcName ,String i_counter){
        StringBuilder builder = new StringBuilder();
        builder.append(funcName);
        builder.append(" ");
        builder.append(i_counter);
        String fullName = builder.toString();
        Integer i = null;
        i = map.get(fullName);
        if(i != null)
            return true;
        else
            return false;
    }
}
