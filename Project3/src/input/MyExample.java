class Test {
    public static void main (String [] args){
        A a ;
        a = new A();
        System.out.println(a.foo(1,2));
    }

}

/*class C {
    int a ;
}*/

class A {
    int a ;

    public int foo(int x ,int y){
        a = 5 ;
        return a ;
    }

    public int bar(int x ,int y , int k){
        return 0 ;
    }
}


/*
class B extends A {
    int x ;

    public int bar(){
        a = 5 ;
        return a ;
    }

    public int foo(){
        return 15 ;
    }
}*/

