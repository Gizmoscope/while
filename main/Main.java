package main;

import frontend.BasicRuleSet;
import frontend.Grammar;
import frontend.IntegerScanner;
import frontend.Parser;
import frontend.NonTerminal;
import frontend.ParseTree2;
import frontend.Parser2;
import frontend.ParsingTable;
import frontend.Rule;
import java.io.IOException;
import java.io.InputStream;
import frontend.Scanner2;
import frontend.StringScanner;
import frontend.Token;
import frontend.Variable;
import java.util.Collection;
import java.util.LinkedList;
import threading.BoundedBuffer;

/**
 *
 * @author markus
 */
public class Main {

    public static void main(String[] args) throws IOException {
        test3();
    }

    private static void test1() throws IOException {
        Scanner2 scanner = new Scanner2(System.in);
        scanner.addSubparser('"', new StringScanner(false));
        IntegerScanner integerParser = new IntegerScanner();
        for (char i = '0'; i <= '9'; i++) {
            scanner.addSubparser(i, integerParser);
        }
        Token t = scanner.nextToken();
        while (t != null) {
            System.err.println(t);
            t = scanner.nextToken();
        }
    }

    private static void test2() {
        NonTerminal e = new NonTerminal("Expr");
        NonTerminal t = new NonTerminal("Term");
        Variable plus = new Token.Symbol("+");
        Variable minus = new Token.Symbol("-");
        Variable id = new Token.Symbol("<id>");
        Collection<Rule> rules = new LinkedList<>();
        rules.add(new Rule(e, t, plus, e));
        rules.add(new Rule(e, t, minus, e));
        rules.add(new Rule(e, t));
        rules.add(new Rule(t, id));

        rules = Grammar.toLL1(rules);
        rules.stream().sorted().forEach(rule -> System.out.println(rule));
        System.out.println(rules.size());

        BasicRuleSet.generateParsingTable(
                rules,
                Grammar.extractNonTerminals(rules),
                Grammar.extractTerminals(rules)
        );
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
    // LL(1)-Recompiler
    //   |
    //   | Buffer
    //   v
    // ToASTCompiler
    //   |
    //   | Buffer
    //   v
    // Compiler / Interpreter
    private static void test3() throws IOException {
        ParsingTable table = BasicRuleSet.generateParsingTable(
                BasicRuleSet.BASIC_RULES,
                Grammar.extractNonTerminals(BasicRuleSet.BASIC_RULES),
                Grammar.extractTerminals(BasicRuleSet.BASIC_RULES));
        InputStream consoleToScanner = System.in;
        
        Scanner2 scanner = new Scanner2(consoleToScanner);
        BoundedBuffer<Scanner2.ScanObject> scannerToParser = new BoundedBuffer<>(32);
        scanner.startScan(scannerToParser);
        
        Parser2 parser = new Parser2(scannerToParser, table);
        BoundedBuffer<Parser2.ParseObject> parserToParseTree = new BoundedBuffer<>(32);
        parser.parse(BasicRuleSet.STM, parserToParseTree);
        
        ParseTree2 parseTree = new ParseTree2(parserToParseTree);
        BoundedBuffer parseTreeToAST = new BoundedBuffer(32);
        parseTree.start(parseTreeToAST);
    }
}
