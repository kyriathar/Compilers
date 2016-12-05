package vmapping;

import mapping.Cclass;
import mapping.MySymbol;
import mapping.MySymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hduser on 6/5/2016.
 */




public class ClassContainer {
    private List<Class> classes ;
    private Map<String ,Class> classMap ;

    public ClassContainer(){
        classes = new ArrayList<>();
        classMap = new HashMap<>();
    }

    public List<Class> getClasses() {
        return classes;
    }

    public Class getClass(String name){
        return classMap.get(name);
    }

    public void createClasses(MySymbolTable initTable){
        boolean ignoreFirstClass = true ;
        Class myClass ;
        MySymbolTable currTable ;
        List<MySymbol> classList = initTable.getClassList();

        for(MySymbol mySymbol : classList){              //gia kathe klasi
            if(ignoreFirstClass){          //ignore main class...
                ignoreFirstClass = false ;
            }else {
                myClass = new Class();
                myClass.setName(mySymbol.getName());
                String motherName = ((Cclass) mySymbol).getmotherClass();
                if (motherName != null) {
                    //uparxei mana
                    Class motherClass = classMap.get(motherName);
                    if (motherClass != null) {
                        myClass.inherit(motherClass.getFields(), motherClass.getVtable());
                    } else {
                        System.out.println("ClassContainer :: CreateClasses :: motherClass = null ! tha prepe na uparxei ! ");
                    }
                }
                currTable = initTable.enter(mySymbol.getName());
                myClass.setFields(currTable.getFieldList());
                myClass.setVtable(currTable.getMethodList());
                classes.add(myClass);
                classMap.put(myClass.getName(), myClass);
            }
        }
    }
}
