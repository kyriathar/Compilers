package vmapping;

import mapping.MySymbol;

import java.util.List;

/**
 * Created by hduser on 7/5/2016.
 */
public class Class{
    private String name ;
    private Fields fields ;
    private Vtable vtable ;
    private int size ;

    public Class(){
        name = null ;
        fields = new Fields();
        vtable = new Vtable();
        size = 0 ;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Fields getFields() {
        return fields;
    }

    public Vtable getVtable() {
        return vtable;
    }

    // my functions
    public int size(){
        return this.size*4;
    }

    public void setFields(List<MySymbol> fieldList){        //vars
        Fcell fcell ;
        for(MySymbol mySymbol : fieldList ){
            fcell = new Fcell();
            fcell.className = name ;                        //Prosoxi --> prwta setareis to name
            fcell.varName = mySymbol.getName();
            fields.addFcell(fcell);
        }
        fields.createFullNameList();
        size += fields.size();
    }

    public void setVtable(List<MySymbol> methodList){       //functions
        Vcell vcell ;
        vtable.setName(new String( name + "vTable"));
        for(MySymbol mySymbol : methodList ){
            vcell = new Vcell();
            vcell.className = name ;                        //Prosoxi --> prwta setareis to name
            vcell.functionName = mySymbol.getName();
            vtable.addVcell(vcell);
        }
        vtable.createFullNameList();
        size +=1 ;
    }

    public void inherit(Fields fields , Vtable vtable){
        this.fields.clone(fields) ;
        this.vtable.clone(vtable) ;
    }

}
