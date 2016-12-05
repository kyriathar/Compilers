package mapping;

/**
 * Created by hduser on 8/4/2016.
 */
public abstract class MySymbol
{
    protected String name ;
    protected MySymbolTable myTable ;

    public MySymbol(){}

    public MySymbol(String name){
        this.name = name ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMySymbolTable(MySymbolTable myTable){
        System.out.println("MySymbol :: setMySymbolTable()");
    }

    public MySymbolTable getMyTable() {
        return myTable;
    }

    public void setReturnType(String returnType) {}
    public String getReturnType() {return "MySymbol::getReturnType()::ERROR";}
}






