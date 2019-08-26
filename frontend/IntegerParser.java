package frontend;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;

/**
 *
 * @author markus
 */
public class IntegerParser implements Scanner2.Subparser {

    @Override
    public Object[] readToken(int entryCodePoint, Reader input) throws IOException {
        BigInteger value = BigInteger.valueOf(entryCodePoint - '0');
        int columns = 0;
        int next = input.read();
        while('0' <= next && next <= '9') {
            value = value.multiply(BigInteger.TEN).add(BigInteger.valueOf(next - '0'));
            columns++;
            next = input.read();
        }
        return new Object[]{new Token.Number(value), 0, columns, next};
    }
    
}
