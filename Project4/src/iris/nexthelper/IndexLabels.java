package nexthelper;

import java.util.Map;

/**
 * Created by kyriakos on 30/5/2016.
 */
public class IndexLabels {
    public static Map<String,Integer> map;      //thewrw oti ta labels dinontai me auskonta arithmo opws stin askisi 3
    public static Map<String ,Integer> last_instr_map ;     //func name

    public static void put(String procedureName,String labelName ,Integer i_counter){
        String fullName = procedureName+"_"+labelName;
        map.put(fullName,i_counter);
    }

    public static int getIcounter(String procedureName,String labelName ){
        String fullName = procedureName+"_"+labelName;
        return map.get(fullName);
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
