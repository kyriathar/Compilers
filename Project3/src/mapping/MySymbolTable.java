package mapping;

import vmapping.Class;
import vmapping.ClassContainer;

import java.util.*;

/**
 * Created by hduser on 8/4/2016.
 */
public class MySymbolTable {
    private String classScope;
    private MySymbolTable parent;
    private Map<String, MySymbol> map;          //vars & functions & classes
    private List<MySymbol> fieldList ;
    private List<MySymbol> methodList ;
    private List<MySymbol> classList ;

    //CO-arguments
    private String leftOrRight ;
    private boolean msgSend ;

    public MySymbolTable() {
        classScope = null;
        map = new HashMap<>();
        fieldList = new ArrayList<>();
        methodList = new ArrayList<>();
        classList = new ArrayList<>();

        leftOrRight = null ;
        msgSend = false ;
    }

    /*Getters-Setters*/
    public void setParent(MySymbolTable parent) {
        this.parent = parent;
    }

    public MySymbolTable getParent() {
        return parent;
    }

    public HashMap<String, MySymbol> getMap() {
        return (HashMap<String, MySymbol>) map;
    }

    public void setClassScope(String classScope) {
        this.classScope = classScope;
    }

    public List<MySymbol> getClassList() {
        return classList;
    }

    public List<MySymbol> getFieldList() {
        return fieldList;
    }

    public List<MySymbol> getMethodList() {
        return methodList;
    }

    public String getClassScope() {
        return classScope;
    }

    public String getLeftOrRight() {
        return leftOrRight;
    }

    public void setLeftOrRight(String leftOrRight) {
        this.leftOrRight = leftOrRight;
    }

    public boolean getMsgSend() {
        return msgSend;
    }

    public void setMsgSend(boolean msgSend) {
        this.msgSend = msgSend;
    }

    /*my functions*/
    public MySymbol getSymbol(String name){
        return map.get(name);
    }


    private void addToList(MySymbol mySymbol){
        if(mySymbol instanceof Var)
            fieldList.add(mySymbol);
        if(mySymbol instanceof Function)
            methodList.add(mySymbol);
        if(mySymbol instanceof Cclass)
            classList.add(mySymbol);
    }

    public void insert(String name, MySymbol symbol) {     //vale symbol sto symbolTable
        MySymbolTable childTable = new MySymbolTable();
        childTable.setParent(this);                         //set parent
        if (symbol instanceof Cclass)
            childTable.setClassScope(name);             //class scope mono se Klash ! :-)
        symbol.setMySymbolTable(childTable);
        addToList(symbol);
        map.put(name, symbol);
    }

    public MySymbolTable enter(String name) {
        return map.get(name).getMyTable();
    }

    public MySymbol findField(String fieldName){
        MySymbol fieldSymbol ;

        MySymbolTable classTable = this;
        classTable = classTable.getParent();            //edw eimai stin klasi mou
        String classScope = classTable.getClassScope(); //gia argotera an xriastei na psaksw se mana

        fieldSymbol = classTable.getSymbol(fieldName);
        if(fieldSymbol!=null){                  //uparxei ws Field stin klasi mou
            ((Var)fieldSymbol).setClassScope(classScope);
            return fieldSymbol;
        }

        classTable = classTable.getParent();        //stis klaseis
        Cclass myClass = (Cclass) classTable.getSymbol(classScope);
        while (myClass.motherClass != null) {                               //oso i current class exei mana
            String myMother = myClass.getmotherClass();
            Cclass motherClass = (Cclass) classTable.getSymbol(myMother);
            MySymbolTable motherTable = motherClass.getMyTable();
            fieldSymbol = motherTable.getSymbol(fieldName);
            if(fieldSymbol!=null){                  //uparxei ws Field stin klasi mou
                classScope = motherTable.getClassScope();
                ((Var)fieldSymbol).setClassScope(classScope);
                return fieldSymbol;
            } else {
                myClass = (Cclass) classTable.getSymbol(motherClass.getName());    //de vrikame idia sinartisi sti mana pame sti giagia kok
            }
        }
        return null;
    }

    public String findMethodRetunType(String className ,String methodName){
        MySymbolTable initTable = this;
        while(initTable.getParent() != null){          //get root table ---> classes
            initTable = initTable.getParent() ;
        }

        MySymbolTable currSymbolTable = initTable.enter(className) ;
        MySymbol func = currSymbolTable.getSymbol(Function.WrapFunc(methodName));
        if(func != null){
            return func.getReturnType();        //uparxei i sinartisi stin proswrini klasi
        }

        String classScope = currSymbolTable.getClassScope();        //psakse stis manes ktl
        MySymbolTable classTable = currSymbolTable.getParent();
        Cclass myClass = (Cclass) classTable.getSymbol(classScope);
        while (myClass.motherClass != null) {                               //oso i current class exei mana
            String myMother = myClass.getmotherClass();
            Cclass motherClass = (Cclass) classTable.getSymbol(myMother);
            MySymbolTable motherTable = motherClass.getMyTable();
            func = motherTable.getSymbol(Function.WrapFunc(methodName));
            if(func!=null){                  //uparxei i func stin klasi mou
                return func.getReturnType();
            } else {
                myClass = (Cclass) classTable.getSymbol(motherClass.getName());    //de vrikame idia sinartisi sti mana pame sti giagia kok
            }
        }

        return null;
    }

    public void mySetLeftOrRight(String leftOrRight){
        MySymbolTable initTable = this;
        while(initTable.getParent() != null){          //get root table ---> classes
            initTable = initTable.getParent() ;
        }
        initTable.setLeftOrRight(leftOrRight);
    }

    public String myGetLeftOrRight(){
        MySymbolTable initTable = this;
        while(initTable.getParent() != null){          //get root table ---> classes
            initTable = initTable.getParent() ;
        }
        return initTable.getLeftOrRight();
    }

    public void mySetmsgSend(boolean msgSend){
        MySymbolTable initTable = this;
        while(initTable.getParent() != null){          //get root table ---> classes
            initTable = initTable.getParent() ;
        }
        initTable.setMsgSend(msgSend);
    }

    public boolean myGetMsgSend(){
        MySymbolTable initTable = this;
        while(initTable.getParent() != null){          //get root table ---> classes
            initTable = initTable.getParent() ;
        }
        return initTable.getMsgSend();
    }

    public int findMaxArguments(){
        int maxArgs = 0 ;
        int curArgs = 0 ;

        MySymbolTable initTable = this;
        while(initTable.getParent() != null){          //get root table ---> classes
            initTable = initTable.getParent() ;
        }

        MySymbolTable methodTable = null ;

        for(MySymbol myClass : initTable.getClassList()){
            methodTable = myClass.getMyTable();
            for(MySymbol myMethod : methodTable.getMethodList()){
                curArgs = ((Function)myMethod).getArgs().size();
                if(curArgs > maxArgs)
                    maxArgs = curArgs ;
            }
        }

        return maxArgs ;
    }

}