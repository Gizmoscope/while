package vvhile.main;

import vvhile.basic.language.BasicRuleSet;
import vvhile.frontend.RuleSet;
import vvhile.frontend.Grammar;
import vvhile.basic.language.IntegerScanner;
import vvhile.frontend.NonTerminal;
import vvhile.frontend.ParseTree;
import vvhile.frontend.Parser;
import vvhile.frontend.ParsingTable;
import vvhile.frontend.Rule;
import java.io.IOException;
import vvhile.frontend.Scanner;
import vvhile.basic.language.StringScanner;
import vvhile.frontend.Token;
import java.util.Collection;
import vvhile.util.BoundedBuffer;
import vvhile.intrep.ASTCompiler;
import java.io.InputStream;
import java.lang.reflect.Field;
import vvhile.basic.language.BasicTokens;
import vvhile.hoare.BooleanFormula;
import vvhile.hoare.HoareProver;
import vvhile.hoare.HoareTree;
import vvhile.hoare.HoareTriple;
import vvhile.intrep.Expression;
import vvhile.intrep.Statement;

/**
 *
 * @author markus
 */
public class Main {

    public static void main(String[] args) throws IOException {
        //System.out.println(Parser.parseExpression("(X = Y)"));
        test2();
    }

    private static final RuleSet ruleSet = new BasicRuleSet();

    private static Scanner defaultScanner() throws IOException {
        Scanner scanner = Scanner.getDefaultScanner(
                "while (!(X = Y)) {\n"
                + "    if (X > Y) {\n"
                + "        X := X - Y\n"
                + "    } else {\n"
                + "        Y := Y - X\n"
                + "    }\n"
                + "}");
        return scanner;
    }

    private static void test2() throws IOException {
        Collection<Rule> rules = ruleSet.getRules();
//        rules.stream().sorted().forEach(rule -> System.out.println(rule));
        rules = Grammar.eliminateRecursion(rules);
        rules = Grammar.toLL1(rules);
        System.out.println();
//        rules.stream().sorted().forEach(rule -> System.out.println(rule));
        Expression.Constant.showSorts = false;
        Expression.Variable.showSorts = false;

        ParsingTable table = Grammar.generateParsingTable(
                rules,
                Grammar.extractNonTerminals(rules),
                Grammar.extractTerminals(rules),
                BasicRuleSet.STM
        );
        //System.out.println(table);

        test3(table, BasicRuleSet.STM);
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
    private static void test3(ParsingTable table, NonTerminal start) throws IOException {
        InputStream consoleToScanner = System.in;

        Scanner scanner = defaultScanner();
        BoundedBuffer<Scanner.ScanObject> scannerToParser = new BoundedBuffer<>(32);
        scanner.startScan(scannerToParser);

        Parser parser = new Parser(scannerToParser, ruleSet, BasicRuleSet.STM);
        BoundedBuffer<Parser.ParseObject> parserToParseTree = new BoundedBuffer<>(32);
        parser.startParsing(start, parserToParseTree);

        ParseTree parseTree = new ParseTree(parserToParseTree);
        BoundedBuffer<Parser.ParseObject> parseTreeToASTCompiler = new BoundedBuffer<>(32);
        parseTree.startRetranslation(parseTreeToASTCompiler);

        ASTCompiler astCompiler = new ASTCompiler(parseTreeToASTCompiler, ruleSet);
        BoundedBuffer<Parser.ParseObject> astCompilerToInterpreter = new BoundedBuffer<>(32);
        astCompiler.startCompilation(astCompilerToInterpreter);

        scanner.join();
        parser.join();
        parseTree.join();
        astCompiler.join();

//        System.out.println(astCompiler.toString());
        Statement statement = (Statement) astCompiler.getRoot();
//        State state = new State();
//        while (s != null) {
//            s = s.run(state).getProgram();
//            System.out.println(state);
//        }
        HoareTree hoareTree = new HoareProver().buildHoareTree(
                new HoareTriple(new BooleanFormula.BlackBox("pre"),
                        statement,
                        new BooleanFormula.BlackBox("post")
                )
        );
        System.out.println(hoareTree.toProofOutline().reduce().toString(true));
    }
}
