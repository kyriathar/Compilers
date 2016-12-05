class Test {
    public static void main (String [] args){
        //C c ;
        //D d ;
        A a ;
        int x ;
        //d = new D();
        //x = (d.getI()).getI();
        //System.out.println(x);
        a = new A();
        x = a.setI(30);
        x = a.compareWithMe(20);
        System.out.println(x);
    }
}

class A{
    int i ;

    public int setI(int x){
        i = x ;
        return x ;
    }

    public int compareWithMe(int x){
        int z ;
        z = i < x;
        return z ;
    }
}



/*
class D {
    C c ;

    public C getI(){
        c = new C();
        return c ;
    }
}

class C {
    int i ;

    public int getI(){
        i = 5;
        return i;
    }
}
*/