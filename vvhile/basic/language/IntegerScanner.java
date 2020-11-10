package vvhile.basic.language;

import vvhile.frontend.Scanner.ScanObject;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import vvhile.frontend.Scanner;
import vvhile.frontend.Token;
import vvhile.intrep.Expression;

/**
 * An IntegerScanner is a Subscanner that scans sequences of digits until a
 * non-digit and returns the number token obtained by it.
 *
 * @author markus
 */
public class IntegerScanner implements Scanner.Subscanner {

    /**
     * Creates an integer scanner. It assumes that the entry character is a
     * digit and therefore only scans positive integers. Integers can be
     * arbitrarily large as they are stored as a BigInteger.
     */
    public IntegerScanner() {
    }

    @Override
    public ScanObject readToken(int entryCodePoint, Reader input) throws IOException {
        // The first digit is given by the entryCharacter
        BigInteger value = BigInteger.valueOf(entryCodePoint - '0');
        int columns = 0;
        int next = input.read();
        // Read as long next is a digit
        while ('0' <= next && next <= '9') {
            // append the digit to the value
            value = value.multiply(BigInteger.TEN).add(BigInteger.valueOf(next - '0'));
            columns++;
            next = input.read();
        }
        return new ScanObject(new Number(value), 0, columns, next);
    }

    /**
     * A number is a token representing an integer value.
     */
    public static class Number implements Token {

        private final BigInteger value;

        public Number(BigInteger value) {
            this.value = value;
        }

        /**
         * @return the internal integer value
         */
        public BigInteger getValue() {
            return value;
        }

        /**
         * Convert this token into a constant representing the integer value of
         * this token.
         * 
         * @return the token as a constant
         */
        public Expression.Constant getConstant() {
            return new Expression.Constant(Expression.SORT_INTEGER, value);
        }

        @Override
        public String toString() {
            return "<num,\"" + value + "\">";
        }

        @Override
        public int hashCode() {
            return 2;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Number;
        }
    }

}
