class Classes {
	public static void main(String[] a) {
		Base b;
		Derived d;
  		b = new Base();
 		d = new Derived();
		b = d;
		System.out.println(b.set(1));
		System.out.println(b.set(3));
	}
}

class Base {
	int data;
	public int set(int x) {
		data = x;
		return data;
	}
	public int get() {
		return data;
	}
	
	public int foo(int x, int y, int z){
		return x;
	}
}

class Derived extends Base {
	Base asd;
	public int set(int x) {
		x = 10;
		asd = new Derived();
	    x = asd.foo(asd.foo(x,asd.foo(x,x,asd.foo(x,x,x)),x),x,x);
		data = x * 2;
		return data;
	}
}
