package vvhile.main;

import vvhile.basic.language.BasicRuleSet;
import vvhile.frontend.RuleSet;
import vvhile.frontend.ParseTree;
import vvhile.frontend.Parser;
import java.io.IOException;
import vvhile.frontend.Scanner;
import vvhile.util.BoundedBuffer;
import vvhile.intrep.ASTCompiler;
import vvhile.hoare.BooleanFormula;
import vvhile.hoare.HoareProver;
import vvhile.hoare.HoareTree;
import vvhile.hoare.HoareTriple;
import vvhile.intrep.Expression;
import vvhile.intrep.Statement;

/**
 * So far this class is only a place to do some testing. Run the main method to
 * compile and verify a simple program. The method shows how to use the
 * compilation pipeline. You can easily change the process to compile and verify
 * your own program.
 *
 * @author markus
 */
public class Main {

    private static Scanner defaultScanner() throws IOException {
        Scanner scanner = Scanner.getDefaultScanner(
                "while (X>0) {\n"
                + "    X := X - 1"
                + "}");
        return scanner;
    }

    // Compile Chain:
    // Input
    //   |
    //   | BufferedReader
    //   v
    // Scanner
    //   |
    //   | Buffer
    //   v 
    // Parser
    //   |
    //   | Buffer
    //   v
    // ParseTree
    //   |
    //   | Buffer
    //   v
    // ToASTCompiler
    //   |
    //   | Buffer
    //   v
    // Compiler / Interpreter
    public static void main(String[] args) throws IOException {
        Expression.Constant.showSorts = false;
        Expression.Variable.showSorts = false;

        RuleSet ruleSet = new BasicRuleSet();

//        InputStream consoleToScanner = System.in;
//        Scanner scanner = new Scanner(consoleToScanner);
        // Start scanner
        Scanner scanner = defaultScanner();
        BoundedBuffer<Scanner.ScanObject> scannerToParser = new BoundedBuffer<>(32);
        scanner.startScan(scannerToParser);

        // Start parser
        Parser parser = new Parser(scannerToParser, ruleSet, BasicRuleSet.STM);
        BoundedBuffer<Parser.ParseObject> parserToParseTree = new BoundedBuffer<>(32);
        parser.startParsing(BasicRuleSet.STM, parserToParseTree);

        // Start parse tree
        ParseTree parseTree = new ParseTree(parserToParseTree);
        BoundedBuffer<Parser.ParseObject> parseTreeToASTCompiler = new BoundedBuffer<>(32);
        parseTree.startRetranslation(parseTreeToASTCompiler);

        // start AST compiler
        ASTCompiler astCompiler = new ASTCompiler(parseTreeToASTCompiler, ruleSet);
        BoundedBuffer<Parser.ParseObject> astCompilerToInterpreter = new BoundedBuffer<>(32);
        astCompiler.startCompilation(astCompilerToInterpreter);

        // wait for the started threads
        scanner.join();
        parser.join();
        parseTree.join();
        astCompiler.join();

        // get the statement
        Statement statement = (Statement) astCompiler.getRoot();

        // --- This was the compilation ---
        // --- Do some verification ---
        // build the Hoare tree
        HoareProver prover = new HoareProver();
        HoareTree hoareTree = prover.buildHoareTree(
                new HoareTriple((BooleanFormula) Parser.parseExpression("X >= 0"),
                        statement,
                        (BooleanFormula) Parser.parseExpression("X = 0")
                )
        );

        // determine the loop invariant black boxes...
        BooleanFormula.BlackBox invariant = prover.getBlackBoxes().get(0);
        // ...and fill them using your cleverness
        hoareTree = hoareTree.fillBlackBox(invariant, (BooleanFormula) Parser.parseExpression("X >= 0"));

        // Inspect the hoare tree with your own loop invariant
        System.out.println("Hoare Tree:");
        System.out.println(hoareTree);
        System.out.println();

        // Get a list of all obligations
        System.out.println("Obligations:");
        hoareTree.getObligations().forEach(
                obligation -> System.out.println(obligation)
        );
    }
}
