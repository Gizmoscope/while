package vvhile.basic.operators;

import java.math.BigInteger;

/**
 *
 * @author markus
 */
public class Plus extends Operator<BigInteger> {

    public static final Plus PLUS = new Plus();

    private Plus() {
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{BigInteger.class, BigInteger.class};
    }

    @Override
    public BigInteger evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        BigInteger a = (BigInteger) args[0];
        BigInteger b = (BigInteger) args[1];
        return a.add(b);
    }

    @Override
    public String toString() {
        return "+";
    }

    @Override
    public Class getReturnClass() {
        return BigInteger.class;
    }

}
