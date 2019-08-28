package main;

import frontend.BasicRuleSet;
import frontend.Grammar;
import frontend.IntegerScanner;
import frontend.NonTerminal;
import frontend.ParseTree2;
import frontend.Parser2;
import frontend.ParsingTable;
import frontend.Rule;
import java.io.IOException;
import frontend.Scanner2;
import frontend.StringScanner;
import frontend.Token;
import java.util.Collection;
import java.util.LinkedList;
import threading.BoundedBuffer;

/**
 *
 * @author markus
 */
public class Main {

    public static void main(String[] args) throws IOException {
        test2();
    }
    
    private static final NonTerminal e = new NonTerminal("E");
    private static final NonTerminal t = new NonTerminal("T");
    private static final NonTerminal f = new NonTerminal("F");
    private static final Token.Symbol plus = new Token.Symbol("+");
    private static final Token.Symbol minus = new Token.Symbol("-");
    private static final Token.Symbol times = new Token.Symbol("*");

    private static Scanner2 defaultScanner() throws IOException {
        Scanner2 scanner = new Scanner2("4 + 2 - 3");
        scanner.addSymbol(plus);
        scanner.addSymbol(minus);
        scanner.addSubparser('"', new StringScanner(false));
        IntegerScanner integerParser = new IntegerScanner();
        for (char i = '0'; i <= '9'; i++) {
            scanner.addSubparser(i, integerParser);
        }
        return scanner;
    }

    private static void test2() throws IOException {
        
        Collection<Rule> rules = new LinkedList<>();
        rules.add(new Rule(e, t, plus, e));
        rules.add(new Rule(e, t, minus, e));
        rules.add(new Rule(e, t));
        rules.add(new Rule(t, Token.NUM));
        
        rules.stream().sorted().forEach(rule -> System.out.println(rule));
        System.out.println();

        rules = Grammar.toLL1(rules);
        rules.stream().sorted().forEach(rule -> System.out.println(rule));
        System.out.println();
        System.out.println();

        ParsingTable table = BasicRuleSet.generateParsingTable(
                rules,
                Grammar.extractNonTerminals(rules),
                Grammar.extractTerminals(rules),
                e
        );
        
        test3(table, e);
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
    private static void test3(ParsingTable table, NonTerminal start) throws IOException {        
        Scanner2 scanner = defaultScanner();
        BoundedBuffer<Scanner2.ScanObject> scannerToParser = new BoundedBuffer<>(32);
        scanner.startScan(scannerToParser);
        
        Parser2 parser = new Parser2(scannerToParser, table);
        BoundedBuffer<Parser2.ParseObject> parserToParseTree = new BoundedBuffer<>(32);
        parser.startParsing(start, parserToParseTree);
        
        ParseTree2 topdown = new ParseTree2(parserToParseTree, true);
        BoundedBuffer<Parser2.ParseObject> topDownToBottomUp = new BoundedBuffer<>(32);
        topdown.startRecompilation(topDownToBottomUp);
        
        ParseTree2 bottomup = new ParseTree2(topDownToBottomUp, false);
        BoundedBuffer<Parser2.ParseObject> bottomUpToNext = new BoundedBuffer<>(32);
        bottomup.startRecompilation(bottomUpToNext);
        
        bottomup.join();
        System.out.println(bottomup);
    }
}
