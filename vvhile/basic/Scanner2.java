package vvhile.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import vvhile.basic.Token.Symbol;

/**
 *
 * @author markus
 */
public class Scanner2 {

    private final TokenTree tokenTree;
    private BufferedReader input;
    private Predicate<Integer> terminator;
    private int next;

    private int row;
    private int column;

    /**
     * Create a new Scanner with empty token tree and whitespaces as only terminators.
     * To use the scanner first add tokens, terminators and (most importantly)
     * an input stream.
     */
    public Scanner2() {
        tokenTree = new TokenTree("");
        terminator = (Integer c) -> {
            return Character.isWhitespace(c);
        };
        next = -1;
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
        if (next == -1) {
            // the stream ended (End Of File)
            return Token.EOF;
        }
        // remember the position in the token tree
        TokenTree position = tokenTree;
        while (position.children.containsKey(next)) {
            // Step into the tree along read characters
            position = position.children.get(next);
            next();
            if (next == -1 || terminator.test(next)) {
                // the stream ended or a terminator interupts the scan
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
     * @param inputStream a new input stream to be read from
     * @throws IOException 
     */
    public void setInput(InputStream inputStream) throws IOException {
        input = new BufferedReader(new InputStreamReader(inputStream));
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

}
