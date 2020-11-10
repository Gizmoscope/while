package vvhile.basic.language;

import vvhile.frontend.Scanner.ScanObject;
import java.io.IOException;
import java.io.Reader;
import vvhile.frontend.Scanner;
import vvhile.frontend.Token;
import vvhile.intrep.Expression;

/**
 * A StringScanner is a Subscanner that scans character sequences beginning and
 * ending in '"'. The terminating symbol '"' can be quoted by a leading '\' or 
 * any other quotation character specified on creation.
 * 
 * @author markus
 */
public class StringScanner implements Scanner.Subscanner {

    // Are strings allowed that extend over multiple lines?
    private final boolean allowLineBreak;
    // Quotation character or -1 for no quotation
    private final int quoteCodePoint;

    /**
     * Creates a string scanner. It assumes that the entry point is '"'. It then
     * scans until it reaches the first unquoted '"' and returns the read 
     * character sequence as a token. 
     * 
     * @param allowLineBreak true if strings are allowed to extend over multible
     * lines
     * @param quoteCodePoint quotation character or -1 if no quotation is wanted
     */
    public StringScanner(boolean allowLineBreak, int quoteCodePoint) {
        this.allowLineBreak = allowLineBreak;
        this.quoteCodePoint = quoteCodePoint;
    }

    /**
     * Creates a string scanner. It assumes that the entry point is '"'. It then
     * scans until it reaches the first unquoted '"' and returns the read 
     * character sequence as a token. 
     * 
     * @param allowLineBreak true if strings are allowed to extend over multible
     * lines
     */
    public StringScanner(boolean allowLineBreak) {
        this(allowLineBreak, '\\');
    }
    
    @Override
    public ScanObject readToken(int entryCodePoint, Reader input) throws IOException {
        StringBuilder builder = new StringBuilder();
        Token token = null;
        int rows = 0;
        int columns = 0;
        int next = input.read();
        // A string always ends in '"'.
        if (next < 0) {
            token = new Token.Error("Reached end of file before string was terminated.");
        } else {
            // In the beginning no quote (default: '\') was read
            boolean quote = false;
            while (quote || next != '"') {
                // handle line break
                if (next == '\n') {
                    rows++;
                    columns = 0;
                    if (!allowLineBreak) {
                        token = new Token.Error("Line break in strings is not allowed.");
                        break;
                    }
                }
                // append the read character check if it is a quote and read the next one
                builder.appendCodePoint(next);
                quote = next == quoteCodePoint;
                next = input.read();
            }
            // If token is not null then there was an error
            if (token == null) {
                // Skip the closing '"'
                next = input.read();
                columns++;
                // create token from string
                token = new StringToken(builder.toString());
            }
        }
        return new ScanObject(token, rows, columns, next);
    }

    /**
     * A string token is a token representing a string.
     */
    public static class StringToken implements Token {

        private final String string;

        /**
         * Create a new string token from the given string.
         * 
         * @param string a string
         */
        public StringToken(String string) {
            this.string = string;
        }

        /**
         * @return the internal string value
         */
        public String getString() {
            return string;
        }

        /**
         * Convert this token into a constant representing the string value of
         * this token.
         * 
         * @return the token as a constant
         */
        public Expression getConstant() {
            return new Expression.Constant(Expression.SORT_STRING, string);
        }

        @Override
        public String toString() {
            return "<string,\"" + string + "\">";
        }

    }
}
