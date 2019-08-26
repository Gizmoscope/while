package main;

import frontend.BasicRuleSet;
import frontend.Grammar;
import frontend.IntegerParser;
import frontend.Parser;
import frontend.NonTerminal;
import frontend.Rule;
import java.io.IOException;
import java.io.InputStream;
import frontend.Scanner2;
import frontend.StringParser;
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
    private static void createChain() {
        InputStream input = System.in;
        Scanner2 scanner = new Scanner2();
        BoundedBuffer bufferScannerParser = new BoundedBuffer<Token>(16);
        // Parser parser = new Parser(parsingTable);
        // parser.setInput(bufferScannerParser);
        scanner.startScan(bufferScannerParser);
        try {
            scanner.setInput(input);
        } catch (IOException e) {
        }

    }

    public static void main(String[] args) throws IOException {
        test1();
    }

    private static void test1() throws IOException {
        InputStream input = System.in;
        Scanner2 scanner = new Scanner2();
        scanner.addSubparser('"', new StringParser(false));
        IntegerParser integerParser = new IntegerParser();
        for (char i = '0'; i <= '9'; i++) {
            scanner.addSubparser(i, integerParser);
        }
        scanner.setInput(input);
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
}
