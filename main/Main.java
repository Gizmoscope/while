package main;

import java.io.IOException;
import java.io.InputStream;
import vvhile.basic.Scanner2;
import vvhile.basic.Token;

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
        InputStream input = System.in;
        Scanner2 scanner = new Scanner2();
        scanner.setInput(input);
        Token t = scanner.nextToken();
        while(t != null) {
            System.err.println(t);
            t = scanner.nextToken();
        }
    }
}
