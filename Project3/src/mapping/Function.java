package mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hduser on 8/4/2016.
 */
public class Function extends MySymbol {
    protected String returnType ;
    protected List<String> args ;

    public Function(){
        this.args = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getArgs() {
        return args;
    }

    public void addArg(String arg) {
        args.add(arg);
    }

    public void setMySymbolTable(MySymbolTable myTable){
        this.myTable = myTable ;
    }

    public static String WrapFunc(String name){
        return name + " F";
    }

    public static String UnWrapFunc(String name){
        String[] splited = name.split(" ");
        return splited[0];
    }
}
