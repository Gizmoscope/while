package vvhile.frontend;

import vvhile.basic.language.IntegerScanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import vvhile.frontend.Token.Symbol;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import vvhile.basic.language.BasicRuleSet;
import vvhile.util.BoundedBuffer;

/**
 * A scanner reads a stream of characters and turns them into a stream of
 * tokens. The behavior of the scanner can be influenced by a set of predefiend
 * symbols and a set of so-called subscanners.
 *
 * @author markus
 */
public class Scanner {

    /**
     * Returns a Scanner based on the basic rule set. It scans all tokens
     * appearing in that grammar and scans integers. It terminates the scan for
     * identifiers if special symbols, such as parentheses, appear.
     * 
     * @param input an input string
     * @return default scanner for that input
     */
    public static Scanner getDefaultScanner(String input) {
        try {
            RuleSet grammar = new BasicRuleSet();
            Scanner scanner = new Scanner(input, grammar);
            scanner.addTerminator(List.of(
                    Integer.valueOf('~'),
                    Integer.valueOf(';'),
                    Integer.valueOf(':'),
                    Integer.valueOf('('),
                    Integer.valueOf(')'),
                    Integer.valueOf('{'),
                    Integer.valueOf('}'),
                    Integer.valueOf('<'),
                    Integer.valueOf('='),
                    Integer.valueOf('>'),
                    Integer.valueOf('+'),
                    Integer.valueOf('-'),
                    Integer.valueOf('*'),
                    Integer.valueOf('/'),
                    Integer.valueOf('&'),
                    Integer.valueOf('|'),
                    Integer.valueOf('^'),
                    Integer.valueOf('?'),
                    Integer.valueOf('.'),
                    Integer.valueOf(',')
            ));
            return scanner;
        } catch (IOException e) {
            return null;
        }
    }

    private final TokenTree tokenTree;
    private Reader input;
    private Predicate<Integer> terminator;
    private Map<Integer, Subscanner> subscanners;
    private int next;

    private int row;
    private int column;

    private Thread thread;

    /**
     * Create a new Scanner with empty token tree and whitespaces as only
     * terminators. To use the scanner first add tokens, terminators and
     * subscanners.
     *
     * @param inputStream an input stream to be read from
     * @param ruleSet a rule set
     * @throws java.io.IOException
     */
    public Scanner(InputStream inputStream, RuleSet ruleSet) throws IOException {
        tokenTree = new TokenTree("");
        subscanners = new HashMap<>();
        terminator = (Integer c) -> {
            return Character.isWhitespace(c);
        };
        setInput(inputStream);
        Grammar.extractTerminals(ruleSet.getRules()).stream().forEach(token -> {
            if (token instanceof Symbol) {
                addSymbol((Symbol) token);
            }
        });
        for (char c = '0'; c <= '9'; c++) {
            addSubscanner(c, new IntegerScanner());
        }
    }

    /**
     * Create a new Scanner with empty token tree and whitespaces as only
     * terminators. To use the scanner first add tokens, terminators and
     * subscanners.
     *
     * @param inputStream an input stream to be read from
     * @throws java.io.IOException
     */
    public Scanner(InputStream inputStream) throws IOException {
        tokenTree = new TokenTree("");
        subscanners = new HashMap<>();
        terminator = (Integer c) -> {
            return Character.isWhitespace(c);
        };
        setInput(inputStream);
    }

    /**
     * Create a new Scanner with empty token tree and whitespaces as only
     * terminators. To use the scanner first add tokens, terminators and
     * subscanners.
     *
     * @param input an input string to be read from
     * @throws java.io.IOException
     */
    public Scanner(String input) throws IOException {
        tokenTree = new TokenTree("");
        subscanners = new HashMap<>();
        terminator = (Integer c) -> {
            return Character.isWhitespace(c);
        };
        setInput(new StringReader(input));
    }

    /**
     * Create a new Scanner with empty token tree and whitespaces as only
     * terminators. To use the scanner first add tokens, terminators and
     * subscanners.
     *
     * @param input an input string to be read from
     * @param ruleSet a rule set
     * @throws java.io.IOException
     */
    public Scanner(String input, RuleSet ruleSet) throws IOException {
        tokenTree = new TokenTree("");
        subscanners = new HashMap<>();
        terminator = (Integer c) -> {
            return Character.isWhitespace(c);
        };
        setInput(new StringReader(input));
        Grammar.extractTerminals(ruleSet.getRules()).stream().forEach(token -> {
            if (token instanceof Symbol) {
                addSymbol((Symbol) token);
            }
        });
        for (char c = '0'; c <= '9'; c++) {
            addSubscanner(c, new IntegerScanner());
        }
    }

    /**
     * Read the next token from the input stream. The token will be found in the
     * token tree or is an identifyer, otherwise.
     *
     * @return next token
     * @throws IOException
     */
    public Token nextToken() throws IOException {
        skipWhitespaces();
        if (next < 0) {
            // the stream ended (End Of File)
            return Token.EOF;
        }
        // trigger subscanner if next is an entryCharacter
        if (subscanners.containsKey(next)) {
            ScanObject result = subscanners.get(next).readToken(next, input);
            int rows = result.getLine();
            int cols = result.getPositionInLine();
            next = result.getCodePoint();
            if (rows == 0) {
                column += cols;
            } else {
                row += rows;
                column = cols;
            }
            return result.getToken();
        }
        // remember the position in the token tree
        TokenTree position = tokenTree;
        while (position.children.containsKey(next)) {
            // Step into the tree along read characters
            position = position.children.get(next);
            next();
            if (next < 0 || terminator.test(next) && !position.children.containsKey(next)) {
                // the stream ended or a terminator interupts the scan for identifiers
                // check wether the last part is a token or not (i.e. identifier)
                if (position.token != null) {
                    return position.token;
                } else {
                    return new Token.Identifier(position.begin);
                }
            }
        }
        // Here, the token tree didn't provide any longer path
        if (position.token != null) {
            // There is a valid token in the tree -> return it
            return position.token;
        } else {
            // No valid token -> read an identifier instead
            StringBuilder builder = new StringBuilder(position.begin);
            while (!terminator.test(next)) {
                builder.appendCodePoint(next);
                next();
                if (next == -1) {
                    // the stream ended
                    break;
                }
            }
            return new Token.Identifier(builder.toString());
        }
    }

    /**
     * Add a token to the token tree of the scanner. If another token with the
     * same input string is already contained in the token tree, there will be
     * thrown an exeption.
     *
     * @param symbol a symbol token
     */
    public void addSymbol(Symbol symbol) {
        TokenTree position = tokenTree;
        // step into the token tree along the input string of the symbol
        String sym = symbol.getSymbol();
        for (int i = 0; i < sym.length(); i++) {
            int c = sym.codePointAt(i);
            // create new child if the path of characters diverges
            if (!position.children.containsKey(c)) {
                position.children.put(c, new TokenTree(sym.substring(0, i + 1)));
            }
            position = position.children.get(c);
        }
        if (symbol.equals(position.token)) {
            // The symbol is already contained in the tree
        } else if (position.token != null) {
            throw new IllegalArgumentException("A different token has the same input string.");
        } else {
            position.token = symbol;
        }
    }

    /**
     * Add a subparser to be triggered on the given entryCharacter.
     *
     * @param entryCodePoint character that triggers the subparser
     * @param subparser the subparser
     */
    public final void addSubscanner(int entryCodePoint, Subscanner subparser) {
        subscanners.put(entryCodePoint, subparser);
    }

    /**
     * A terminator is a predicate that decides whether a character can be part
     * of an identifier. The scanning of an identifier ends when reaching a
     * terminator.
     *
     * @param term a codepoint to add as terminating
     */
    public void addTerminator(int term) {
        // Replace existing terminator by a decorated version
        Predicate old = terminator;
        terminator = (Integer c) -> {
            return old.test(c) || c == term;
        };
    }

    /**
     * A terminator is a predicate that decides whether a character can be part
     * of an identifier. The scanning of an identifier ends when reaching a
     * terminator.
     *
     * @param terms a collection of codepoints to add as terminating
     */
    public void addTerminator(Collection<Integer> terms) {
        // Replace existing terminator by a decorated version
        Predicate old = terminator;
        terminator = (Integer c) -> {
            return old.test(c) || terms.contains(c);
        };
    }

    /*
     * @param inputStream a new input stream to be read from
     * @throws IOException
     */
    private void setInput(InputStream inputStream) throws IOException {
        setInput(new BufferedReader(new InputStreamReader(inputStream)));
    }

    private void setInput(Reader reader) throws IOException {
        input = reader;
        row = 0;
        column = 0;
        next();
    }

    /*
     * Reads until a non-whitespace.
     */
    private void skipWhitespaces() throws IOException {
        while (next != -1 && Character.isWhitespace(next)) {
            if (next == '\n') {
                row++;
                column = 0;
            }
            next();
        }
    }

    /*
     * Read one character, increase the column counter.
     */
    private void next() throws IOException {
        next = input.read();
        column++;
    }

    /**
     * Scanns the stream and passes the token to the buffer. This creates a new
     * thread. The scan ends after reaching the end of file. The scanner then
     * passes Token.EOF to the buffer.
     *
     * @param buffer A bounded buffer
     */
    public void startScan(BoundedBuffer<ScanObject> buffer) {
        Runnable r = () -> {
            try {
                Token token;
                do {
                    token = nextToken();
                    buffer.put(new ScanObject(token, row, column, next));
                } while (token != Token.EOF);
            } catch (IOException e) {
                buffer.put(new ScanObject(new Token.Error(e.getMessage()), row, column, next));
            }
            thread = null;
        };
        // Start the thread
        (thread = new Thread(r, "Scanner")).start();
    }

    /**
     * Wait for the termination of the scan.
     */
    public void join() {
        while (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }
    }

    /*
     * The TokenTree contains all tokens. It is structured by their input strings.
     * An input string gets split into its characters each of which lies in one
     * level of the tree. The token-object sits on the end of the path of characters.
     */
    private class TokenTree {

        private Map<Integer, TokenTree> children;
        // The token can be null. It is only non-null if the path from the root
        // indeed forms the input string of the token
        private Token token;
        // the path from the root to here
        private String begin;

        public TokenTree(String begin) {
            children = new HashMap<>();
            this.begin = begin;
        }

    }

    /**
     * A subscanner can be triggered by a scanner to scan a complicated token
     * that has no finite representation and therefore doesn't fit into the
     * tokentree. Examples are numericals and strings.
     */
    public static interface Subscanner {

        /**
         * Reads a single token from the input.It returns an array of the form
         * {token, rows, columns, codePoint}, where token is the read token,
         * rows counts '\n' the parser read, columns counts the number of
         * characters after the last '\n' and codePoint is the last read
         * character.
         *
         * @param entryCodePoint character that triggers the subparser
         * @param input inputstream to parse from
         * @return the token, the number of rows and columns and the last
         * character that were read
         * @throws java.io.IOException
         */
        public ScanObject readToken(int entryCodePoint, Reader input) throws IOException;

    }

    /**
     * Scan objects are the objects that the scanner passes to the parser. Apart
     * from the actual token it contains information about the position in the
     * code.
     */
    public static class ScanObject {

        private final Token token;
        private final int line;
        private final int positionInLine;
        private final int codePoint;

        /**
         * Creates a new scan object.
         *
         * @param token the token read
         * @param line the line of the token
         * @param positionInLine the position in line of the first charater of
         * the string representing the token
         * @param codePoint the character following the token
         */
        public ScanObject(Token token, int line, int positionInLine, int codePoint) {
            this.token = token;
            this.line = line;
            this.positionInLine = positionInLine;
            this.codePoint = codePoint;
        }

        /**
         * @return the token read
         */
        public Token getToken() {
            return token;
        }

        /**
         * @return the line of the token
         */
        public int getLine() {
            return line;
        }

        /**
         * @return the position in line of the first charater of the string
         * representing the token
         */
        public int getPositionInLine() {
            return positionInLine;
        }

        /**
         * @return the character following the token
         */
        public int getCodePoint() {
            return codePoint;
        }

        @Override
        public String toString() {
            return "" + token + " [" + line + "," + positionInLine + ']';
        }

    }

}
