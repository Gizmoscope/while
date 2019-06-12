package vvhile.basic.operators;

import java.math.BigInteger;

/**
 *
 * @author markus
 */
public class Minus extends Operator<BigInteger> {

    public static final Minus MINUS = new Minus();

    private Minus() {
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
        return a.subtract(b);
    }

    @Override
    public String toString() {
        return "−";
    }

    @Override
    public Class getReturnClass() {
        return BigInteger.class;
    }

}
