package frontend;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;

/**
 * An IntegerScanner is a Subscanner that scans sequences of digits until a
 * non-digit and returns the number token obtained by it.
 * 
 * @author markus
 */
public class IntegerScanner implements Scanner2.Subscanner {

    /**
     * Creates an integer scanner. It assumes that the entry character is a digit
     * and therefore only scans positive integers. Integers can be arbitrarily 
     * large as they are stored as a BigInteger.
     */
    public IntegerScanner() {
    }

    @Override
    public Object[] readToken(int entryCodePoint, Reader input) throws IOException {
        // The first digit is given by the entryCharacter
        BigInteger value = BigInteger.valueOf(entryCodePoint - '0');
        int columns = 0;
        int next = input.read();
        // Read as long next is a digit
        while('0' <= next && next <= '9') {
            // append the digit to the value
            value = value.multiply(BigInteger.TEN).add(BigInteger.valueOf(next - '0'));
            columns++;
            next = input.read();
        }
        return new Object[]{new Token.Number(value), 0, columns, next};
    }
    
}
