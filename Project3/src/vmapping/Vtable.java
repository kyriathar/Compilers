package vmapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hduser on 6/5/2016.
 */

class Vcell {
    public String className ;               //to xriazesai panta
    public String functionName ;
}


public class Vtable {
    private String name ;
    private List<Vcell> vcells ;
    private List<String> fullNameList ;

    public Vtable(){
        name = null;
        vcells = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Vcell> getVcells() {
        return vcells;
    }

    public List<String> getFullNameList() {
        return fullNameList;
    }

    public int getMethodOffset(String name){
        return fullNameList.indexOf(name)*4 ;
    }

    public int getMethodOffset2(String name){
        int i =0 ;
        for(Vcell vcell : vcells){
            if(vcell.functionName.equals(name)){
                break ;
            }
            else {
                i++;
            }
        }

        return i*4 ;
    }

    // my functions
    public void addVcell(Vcell vcell){
        for(Vcell currVcell : vcells){
            if(currVcell.functionName.equals(vcell.functionName)){
                currVcell.className = vcell.className ;            //Override function
                return ;
            }
        }
        vcells.add(vcell);
    }

    public void clone(Vtable vtable){
        Vcell myVcell ;
        for(Vcell vcell : vtable.getVcells()){
            myVcell = new Vcell() ;
            myVcell.className = vcell.className ;
            myVcell.functionName = vcell.functionName ;
            addVcell(myVcell);
            //vcells.add(myVcell);
        }
    }

    public void createFullNameList(){
        fullNameList = new ArrayList<>();
        for(Vcell currVcell : vcells){
            fullNameList.add(new String(currVcell.className + "_" +currVcell.functionName));
        }
    }

    public int getSize(){
        return vcells.size()*4;
    }
}
