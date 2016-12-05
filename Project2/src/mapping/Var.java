package mapping;

/**
 * Created by hduser on 8/4/2016.
 */
public class Var extends MySymbol {

    //protected String kind  = "var";
    protected String returnType ;
    protected boolean arrayInitialized = false ;

    public Var(){}

    public Var(String name) {
        super(name);
        this.returnType = null;
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
}
