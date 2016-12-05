JAVAC=javac
JVM=java
CLASSPATH = lib/iris-0.60.jar:lib/iris-parser-0.60.jar:src/iris/
sources = $(wildcard src/iris/*.java)
classes = $(sources:.java=.class)

all: compile

compile: $(classes)

clean :
	rm -f $(classes)

%.class : %.java
	$(JAVAC) -cp $(CLASSPATH) $<

run-binarytree:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/BinaryTree

run-bubblesort:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/BubbleSort

run-factorial:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/Factorial

run-linearsearch:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/LinearSearch

run-linkedlist:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/LinkedList

run-morethan4:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/MoreThan4

run-quicksort:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/QuickSort

run-treevisitor:
	$(JVM) -cp $(CLASSPATH) Main Iris/generated-facts/TreeVisitor
