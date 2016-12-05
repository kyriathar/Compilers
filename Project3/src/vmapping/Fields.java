package vmapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hduser on 6/5/2016.
 */

class Fcell {
    public String className ;       //to xriazesai panta
    public String varName ;
}


public class Fields {
    private List<Fcell> fcells ;
    private List<String> fullNameList ;

    public Fields(){
        fcells = new ArrayList<>();
    }

    public List<Fcell> getFcells() {
        return fcells;
    }

    public List<String> getFullNameList() {
        return fullNameList;
    }

    public int getFieldOffset(String name){
        return (fullNameList.indexOf(name)*4+4) ;       //+4 gia to V table stin thesi 0
    }

    // my functions
    public int size(){
        return fcells.size();
    }

    public void addFcell(Fcell fcell){
        fcells.add(fcell);
    }

    public void clone(Fields fields){
        Fcell myFcell ;
        for(Fcell fcell : fields.getFcells()){
            myFcell = new Fcell() ;
            myFcell.className = fcell.className ;
            myFcell.varName = fcell.varName ;
            addFcell(myFcell);
        }
    }

    public void createFullNameList(){
        fullNameList = new ArrayList<>();
        for(Fcell currFcell : fcells){
            fullNameList.add(new String(currFcell.className + "_" +currFcell.varName));
        }
    }
}
