package frontend;

import java.io.IOException;
import java.io.Reader;

/**
 * A StringScanner is a Subscanner that scans character sequences beginning and
 * ending in '"'. The terminating symbol '"' can be quoted by a leading '\' or 
 * any other quotation character specified on creation.
 * 
 * @author markus
 */
public class StringScanner implements Scanner2.Subscanner {

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
    public Object[] readToken(int entryCodePoint, Reader input) throws IOException {
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
                token = new Token.StringToken(builder.toString());
            }
        }
        return new Object[]{token, rows, columns, next};
    }
}
