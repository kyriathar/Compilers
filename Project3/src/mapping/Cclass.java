package mapping;

/**
 * Created by hduser on 8/4/2016.
 */
public class Cclass extends MySymbol {
    protected String motherClass ;


    public Cclass(String name) {
        super(name);
        motherClass = null;
    }

    public String getmotherClass() {
        return motherClass;
    }

    public void setmotherClass(String motherClass) {
        this.motherClass = motherClass;
    }

    public void setMySymbolTable(MySymbolTable myTable){
        this.myTable = myTable ;
    }

}
