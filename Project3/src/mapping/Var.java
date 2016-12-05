package mapping;

/**
 * Created by hduser on 8/4/2016.
 */
public class Var extends MySymbol {
    protected String returnType ;
    protected String register ;
    protected String classScope ;


    public Var(){
        register = null ;
        classScope = null ;
    }

    public String getClassScope() {
        return classScope;
    }

    public void setClassScope(String classScope) {
        this.classScope = classScope;
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

    public void setMySymbolTable(MySymbolTable myTable){
        this.myTable = null ;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }
}
