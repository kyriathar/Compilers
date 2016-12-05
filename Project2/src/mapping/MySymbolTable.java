package mapping;

import java.util.*;
import error.LineOfError;

/**
 * Created by hduser on 8/4/2016.
 */
public class MySymbolTable {
    private String classScope ;
    private MySymbolTable parent ;
    private Map<String, MySymbol> map ;

    public MySymbolTable(){
        this.map = new HashMap<>();
        classScope = null ;
    }

    /*Getters-Setters*/
    public MySymbolTable getParent() {
        return parent;
    }

    public void setParent(MySymbolTable parent) {
        this.parent = parent;
    }

    public HashMap<String, MySymbol> getMap() {
        return (HashMap<String, MySymbol>) map;
    }

    public void setMap(HashMap<String, MySymbol> map) {
        this.map = map;
    }

    public void setClassScope(String classScope) {
        this.classScope = classScope;
    }

    public String getClassScope() {
        return classScope;
    }

    /*my functions*/
    private boolean symbolExists(String name) {          //!!!!!mporw na exw idio onoma se var kai sunartisi sto idio epipedo ! na parei symbol
        return map.containsKey(name);
    }

    private boolean VarExists(MySymbol symbol) {        //check if User defined Var exists in defined Classes
        String returnType = ((Var)symbol).getReturnType() ;
        if(returnType.equals("int") || returnType.equals("int[]")  || returnType.equals("boolean")  || returnType==null){
            return true ;
        }

        MySymbolTable classTable = this;
        while(classTable.getParent() != null){          //get root table ---> classes
            classTable = classTable.getParent() ;
        }
        MySymbol symbolExists = classTable.getSymbol(returnType);
        if(symbolExists!=null){
            return true ;
        }else{
            return false;
        }
    }

    public void insert(String name , MySymbol symbol) {     //!!!!!prepei na allaksei gia idio onoma function - var
        if(symbolExists(name)){                             //pianei dublication se identifiers + overloading
            System.out.println("MySymbolTable.insert()::DoubleDeclaration" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
        else if((symbol instanceof Var) && !VarExists(symbol)){
            System.out.println("MySymbolTable.insert()::Type doesn't exist" + " (line " + Integer.toString(LineOfError.second_value)+")");
            throw new RuntimeException();
        }
        else {
            MySymbolTable childTable = new MySymbolTable();
            childTable.setParent(this);                         //set parent
            if(symbol instanceof Cclass)
                childTable.setClassScope(name);
            symbol.setMySymbolTable(childTable);
            map.put(name, symbol);
        }
    }

    private boolean WrongOverride(String name , Function currFunc) {
        MySymbolTable classTable = this;
        while(classTable.parent != null){          //get root table ---> classes
            classTable = classTable.getParent() ;
        }

        Cclass myClass = (Cclass) classTable.getSymbol(this.classScope);
        while (myClass.motherClass != null) {                               //oso i current class exei mana
            String myMother = myClass.getmotherClass();
            Cclass motherClass = (Cclass) classTable.getSymbol(myMother);
            MySymbolTable motherTable = motherClass.getMyTable();
            Function motherFunc = (Function) motherTable.getSymbol(name);
            if (motherFunc != null) {      //mama klasi exei idio onoma sinartisis  => koitame args kai return type na nai idia
                if (motherFunc.getReturnType() != currFunc.getReturnType()) {
                    System.out.println("MySymbolTable.WrongOverride()::different return type in method " + Function.UnWrapFunc(name) + "()" + " (line " + Integer.toString(LineOfError.value)+")");
                    return true;
                } else if (!(motherFunc.getArgs().equals(currFunc.getArgs()))) {
                    System.out.println("MySymbolTable.WrongOverride()::different arguments in method " + Function.UnWrapFunc(name) + "()" + " (line " + Integer.toString(LineOfError.value)+")");
                    return true;
                } else
                    return false;
            } else {
                myClass = (Cclass) classTable.getSymbol(motherClass.getName());    //de vrikame idia sinartisi sti mana pame sti giagia kok
            }
        }
        return false ;
    }

    public void checkLastInsertedFunc(Function currFunc){
        String currName = Function.WrapFunc(currFunc.getName());
        if(WrongOverride(currName,currFunc)){
            System.out.println("MySymbolTable.checkLastInsertedFunc()::Wrong Override" + " (line " + Integer.toString(LineOfError.value)+")");
            throw new RuntimeException();
        }
    }

    public void remove(String name){
        map.remove(name);
    }

    public MySymbolTable enter(String name){
        return map.get(name).getMyTable() ;
    }

    public MySymbol getSymbol(String name){
        return map.get(name);
    }

    public String values(){
        StringBuilder builder = new StringBuilder();
        for(MySymbol s :map.values() ){
            builder.append(s.getName());
            builder.append("\n");
        }
        return builder.toString() ;
    }
    /*functions for TypeCheckVisitor*/

    public String lookUp(String identName){            //gets the identifier - Returns it's type
        //check current function's table
        MySymbol identSymbol=null;
        identSymbol = map.get(identName);
        if(identSymbol!=null){
            return identSymbol.getReturnType() ;           //identifier is here localy  ->Return its type
        }else{
            MySymbolTable classTable = this;
            classTable = classTable.getParent();            //edw eimai stin klasi mou
            String classScope = classTable.getClassScope(); //gia argotera an xriastei na psaksw se mana

            identSymbol = classTable.getSymbol(identName);
            if(identSymbol!=null){                  //uparxei ws Field stin klasi mou
                return identSymbol.getReturnType();
            }

            classTable = classTable.getParent();        //stis klaseis
            Cclass myClass = (Cclass) classTable.getSymbol(classScope);
            while (myClass.motherClass != null) {                               //oso i current class exei mana
                String myMother = myClass.getmotherClass();
                Cclass motherClass = (Cclass) classTable.getSymbol(myMother);
                MySymbolTable motherTable = motherClass.getMyTable();
                //Function motherFunc = (Function) motherTable.getSymbol(name);
                identSymbol = motherTable.getSymbol(identName);
                if(identSymbol!=null){                  //uparxei ws Field stin klasi mou
                    return identSymbol.getReturnType();
                } else {
                    myClass = (Cclass) classTable.getSymbol(motherClass.getName());    //de vrikame idia sinartisi sti mana pame sti giagia kok
                }
            }
        }
        return null;
    }

    public boolean UserDefinedTypeExists(String name){
        MySymbolTable classTable = this;
        while(classTable.getParent() != null){          //get root table ---> classes
            classTable = classTable.getParent() ;
        }
        MySymbol symbolExists = classTable.getSymbol(name);
        if(symbolExists!=null){
            return true ;
        }else{
            return false;
        }
    }

    public boolean SubTypeOf(String childName,String motherName){
        MySymbolTable classTable = this;
        while(classTable.getParent() != null){          //get root table ---> classes
            classTable = classTable.getParent() ;
        }

        Cclass childExists = (Cclass) classTable.getSymbol(childName);
        while(childExists != null) {
            if(motherName.equals(childExists.motherClass)){
                return true ;
            }
            else{
                childName = childExists.getmotherClass();
                childExists = (Cclass) classTable.getSymbol(childName);
            }
        }
        return false ;
    }

    public String getThisScope(){
        //we are in function Table
        MySymbolTable classTable = this ;
        if(classTable.getParent()!=null){
            classTable = classTable.getParent();//function table this one should have classScope
            if(classTable.getClassScope()!=null){
                return classTable.getClassScope();
            }else{
                System.out.println("MySymbolTable.getThisScope()::classScope is Null");
                throw new RuntimeException();
            }
        }else{
            System.out.println("MySymbolTable.getThisScope()::ERROR");
            throw new RuntimeException();
        }
    }

    public MySymbol getFunction(String funcName , String className){
        MySymbolTable classTable = this;
        MySymbolTable rootTable  ;
        while(classTable.getParent() != null){          //get root table ---> classes
            classTable = classTable.getParent() ;
        }
        rootTable = classTable ;
        Function functionSymbol =null;
        Cclass classSymbol = (Cclass) rootTable.getSymbol(className);
        while(classSymbol != null) {
            classTable = classSymbol.getMyTable();          //table tis class
            functionSymbol = (Function) classTable.getSymbol(Function.WrapFunc(funcName));
            if(functionSymbol!=null){           //PREPEI NA MPEI INHERITANCE
                return functionSymbol ;
            }else{
                //den to vrika se mena na psaksw sti mana m
                String motherClassName = classSymbol.getmotherClass();
                classSymbol = (Cclass) rootTable.getSymbol(motherClassName);
            }
        }
        System.out.println("MySymbolTable.getFunction()::method doesn't exist in class" + " (line " + Integer.toString(LineOfError.value)+")");
        throw new RuntimeException();
    }

    public boolean check_arguments_equality(List<String> messageSend,List<String> method){
        String msgSendArg;
        String methodArg;

        if(messageSend.size()!=method.size())
            return false ;

        Iterator<String> itMsgSend = messageSend.iterator();
        Iterator<String> itMethod = method.iterator();

        while(itMsgSend.hasNext()){
            msgSendArg = itMsgSend.next();
            methodArg = itMethod.next();
            if(msgSendArg.equals("int")||msgSendArg.equals("int[]")||msgSendArg.equals("boolean")){
                if(!msgSendArg.equals(methodArg))
                    return false ;
            }else{          //User defined check for Subtype
                if(msgSendArg.equals(methodArg))
                    continue ;
                if(!this.SubTypeOf(msgSendArg,methodArg))
                    return false ;
            }
        }
        return true ;
    }
}
