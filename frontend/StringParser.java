package frontend;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author markus
 */
public class StringParser implements Scanner2.Subparser {

    private final boolean allowLineBreak;

    public StringParser(boolean allowLineBreak) {
        this.allowLineBreak = allowLineBreak;
    }

    @Override
    public Object[] readToken(int entryCodePoint, Reader input) throws IOException {
        StringBuilder builder = new StringBuilder();
        Token token = null;
        int rows = 0;
        int columns = 0;
        int next = input.read();
        if (next == -1) {
            token = new Token.Error("Reached end of file before string was terminated.");
        } else {
            boolean quote = false;
            while (quote || next != '"') {
                if (next == '\n') {
                    rows++;
                    columns = 0;
                    if (!allowLineBreak) {
                        token = new Token.Error("Line break in strings is not allowed.");
                        break;
                    }
                }
                builder.appendCodePoint(next);
                quote = next == '\\';
                next = input.read();
            }
            next = input.read();
            if (token == null) {
                token = new Token.StringToken(builder.toString());
            }
        }
        return new Object[]{token, rows, columns, next};
    }
}
