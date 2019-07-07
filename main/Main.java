package main;

import frontend.Grammar;
import frontend.NonTerminal;
import frontend.Rule;
import java.io.IOException;
import java.io.InputStream;
import frontend.Scanner2;
import frontend.Token;
import frontend.Variable;
import java.util.Collection;
import java.util.LinkedList;

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
    public static void main(String[] args) throws IOException {
//        InputStream input = System.in;
//        Scanner2 scanner = new Scanner2();
//        scanner.setInput(input);
//        Token t = scanner.nextToken();
//        while(t != null) {
//            System.err.println(t);
//            t = scanner.nextToken();
//        }

        NonTerminal e = new NonTerminal("Expr");
        NonTerminal t = new NonTerminal("Term");
        Variable plus = new Token.Symbol("+");
        Variable minus = new Token.Symbol("-");
        Collection<Rule> rules = new LinkedList<>();
        rules.add(new Rule(e, t, plus, e));
        rules.add(new Rule(e, t, minus, e));
        rules.add(new Rule(e, t));
        Grammar.toLL1(rules).stream().sorted().forEach(rule -> System.out.println(rule));
        System.out.println(Grammar.toLL1(rules).size());
    }
}
