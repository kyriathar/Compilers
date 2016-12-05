//package iris;

import filename.FileName;
import optimstructs.ConstantPropagation;
import optimstructs.DeadCode;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBase;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.storage.IRelation;
import syntaxtree.Goal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

/**
 * An example program created for graph path evaluation.
 */
class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Please give directory path.");
            System.exit(-1);
        }

        //Visitors :
        String[] inPath = args[0].split("/");
        StringBuilder builder = new StringBuilder();
        builder.append("input/");
        builder.append(inPath[2]);          //e.g Factorial
        builder.append(".spg");
        String fileName = builder.toString();

        File dir = new File(args[0]);
        dir.mkdir();

        FileInputStream fis = null;
        fis = new FileInputStream(fileName);        //input
        SpigletParser spigletParser = new SpigletParser(fis);
        System.out.println("Program : " + fileName );
        System.out.println("Program parsed successfully.");

        //First Visitor -> instructions vars help nexts
        FirstIrisVisitor firstIrisVisitor = new FirstIrisVisitor();
        Goal root = spigletParser.Goal();
        root.accept(firstIrisVisitor, args[0]);
        //Second Visitor -> next varMove constMove
        SecondIrisVisitor secondIrisVisitor = new SecondIrisVisitor();
        root.accept(secondIrisVisitor, args[0]);
        //Third Visitor -> varUse varDef
        ThirdIrisVisitor thirdIrisVisitor = new ThirdIrisVisitor();
        root.accept(thirdIrisVisitor, args[0]);


        Parser parser = new Parser();

        final String projectDirectory = args[0];
        Map<IPredicate, IRelation> factMap = new HashMap<>();

        /** The following loop -- given a project directory -- will list and read parse all fact files in its "/facts"
         *  subdirectory. This allows you to have multiple .iris files with your program facts. For instance you can
         *  have one file for each relation's facts as our examples show.
         */
        final File factsDirectory = new File(projectDirectory);
        if (factsDirectory.isDirectory()) {
            for (final File fileEntry : factsDirectory.listFiles()) {

                if (fileEntry.isDirectory())
                    System.out.println("Omitting directory " + fileEntry.getPath());

                else {
                    Reader factsReader = new FileReader(fileEntry);
                    parser.parse(factsReader);

                    // Retrieve the facts and put all of them in factMap
                    factMap.putAll(parser.getFacts());
                }
            }
        }
        else {
            System.err.println("Invalid facts directory path");
            System.exit(-1);
        }

        File rulesFile = new File(projectDirectory + "/../../analysis-logic/rules.iris");
        Reader rulesReader = new FileReader(rulesFile);

        File queriesFile = new File(projectDirectory + "/../../queries/queries.iris");
        Reader queriesReader = new FileReader(queriesFile);

        // Parse rules file.
        parser.parse(rulesReader);
        // Retrieve the rules from the parsed file.
        List<IRule> rules = parser.getRules();

        // Parse queries file.
        parser.parse(queriesReader);
        // Retrieve the queries from the parsed file.
        List<IQuery> queries = parser.getQueries();

        // Create a default configuration.
        Configuration configuration = new Configuration();

        // Enable Magic Sets together with rule filtering.
        configuration.programOptmimisers.add(new MagicSets());

        // Create the knowledge base.
        IKnowledgeBase knowledgeBase = new KnowledgeBase(factMap, rules, configuration);


        DeadCode.map = new HashMap<>();
        ConstantPropagation.map = new HashMap<>();

        // Evaluate all queries over the knowledge base.
        for (IQuery query : queries) {
            List<IVariable> variableBindings = new ArrayList<>();
            IRelation relation = knowledgeBase.execute(query, variableBindings);

            // Output the variable bindings.
            System.out.println("\n" + query.toString() + "\n" + variableBindings);


            // Output each tuple in the relation, where the term at position i
            // corresponds to the variable at position i in the variable
            // bindings list.
            for (int i = 0; i < relation.size(); i++) {
                System.out.println(relation.get(i));
                //DeadCode...
                if(query.toString().equals("?- DeadCodeComputation(?m, ?i).")) {
                    String str = relation.get(i).toString();
                    String offParenStr = str.substring(1,str.length()-1 );
                    //System.out.println(offParenStr);
                    List<String> list = Arrays.asList(offParenStr.split(","));
                    //System.out.println(list.get(0).substring(1,list.get(0).length()-1 ));
                    //System.out.println(list.get(1));
                    DeadCode.add(list.get(0).substring(1,list.get(0).length()-1 ),list.get(1));
                }
                //Constant Propagation...
                if(query.toString().equals("?- ConstantPropagation(?m, ?i, ?var, ?value).")) {
                    String str = relation.get(i).toString();
                    String offParenStr = str.substring(1,str.length()-1 );
                    List<String> list = Arrays.asList(offParenStr.split(","));
                    ConstantPropagation.put(list.get(0).substring(1,list.get(0).length()-1 ),list.get(1),list.get(2).substring(2,list.get(2).length()-1 ),list.get(3).substring(1,list.get(3).length() ));
                }
            }
        }

        //Optimize Visitor
        /*file name .spg creation*/
        FileName.createSpg(args[0]);
        OptimizeVisitor optimizeVisitor = new OptimizeVisitor();
        root.accept(optimizeVisitor, args[0]);
    }

}