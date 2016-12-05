package optimstructs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyriakos on 5/6/2016.
 */


class ConstInstruction{
    private Map<String,String> map ;

    public ConstInstruction(){
        map = new HashMap<>();
    }

    public void put(String tempName,String constValue ){
        map.put(tempName,constValue);
    }

    public String get(String tempName){
        return map.get(tempName);
    }
}


public class ConstantPropagation {
    public static  Map<String,ConstInstruction> map ;


    public static void put(String funcName ,String i_counter, String tempName, String constValue ){
        StringBuilder builder = new StringBuilder();
        builder.append(funcName);
        builder.append(i_counter);
        String funcInstr = builder.toString();
        ConstInstruction exists = null ;
        exists = map.get(funcInstr);
        if(exists != null){             //uparxei idi
            exists.put(tempName,constValue);
        }else{                          //den uparxei
            exists = new ConstInstruction();
            exists.put(tempName,constValue);
            map.put(funcInstr,exists);
        }
    }

    public static String get(String funcName ,String i_counter, String tempName){
        StringBuilder builder = new StringBuilder();
        builder.append(funcName);
        builder.append(" ");
        builder.append(i_counter);
        String funcInstr = builder.toString();
        ConstInstruction exists = null ;
        exists = map.get(funcInstr);
        if(exists != null){
            String constValue = null ;
            constValue = exists.get(tempName);
            if(constValue != null){
                //exei ferei to value -> an einai label epistrepse null
                if(isNumeric(constValue)){
                    return constValue ;
                }else {
                    return null ;
                }
            }else{
                return null;
            }
        }else{
            return null ;
        }
        //return null;
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
