import mapping.MySymbolTable;
import syntaxtree.*;

import java.io.*;

class Main {
    public static void main (String [] args){
	if(args.length < 1){
	    System.err.println("Usage: java [MainClassName] [file1] [file2] ... [fileN]");
	    System.exit(1);
	}

	FileInputStream fis = null;
	boolean throw_exception = false ;

	for (String arg : args) {
			try {
				fis = new FileInputStream(arg);        //input
				MiniJavaParser parser = new MiniJavaParser(fis);
				System.out.println("Program : " + arg );
				System.out.println("Program parsed successfully.");

				/*root table*/
				MySymbolTable mySymbolTable = new MySymbolTable();
				mySymbolTable.setParent(null);
				//First Visitor -> Fill Symbol Table with class names
				ReadClassVisitor readClassVisitor = new ReadClassVisitor();
				Goal root = parser.Goal();
				root.accept(readClassVisitor, mySymbolTable);
				//Second Visitor -> Fill Symbol Table with fields methods and method's variables
				ReadVarsFuncVisitor readVarsFuncVisitor = new ReadVarsFuncVisitor();
				root.accept(readVarsFuncVisitor, mySymbolTable);
				//Third Visitor -> Type checking
				TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor();
				root.accept(typeCheckingVisitor, mySymbolTable);            //visit

				System.out.println("no errors");
			} catch (ParseException ex) {
				System.out.println(ex.getMessage());
			} catch (FileNotFoundException ex) {
				System.err.println(ex.getMessage());
			} catch (RuntimeException ex) {
				throw_exception = true;
			} finally {
				try {
					if (fis != null) fis.close();
				} catch (IOException ex) {
					System.err.println(ex.getMessage());
				}
			}
	}//for
	if(throw_exception)
		throw new RuntimeException();
	}//main
}
