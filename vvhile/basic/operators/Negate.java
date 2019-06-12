package vvhile.basic.operators;

import java.math.BigInteger;

/**
 *
 * @author markus
 */
public class Negate extends Operator<BigInteger> {

    public static final Negate NEGATE = new Negate();

    private Negate() {
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{BigInteger.class};
    }

    @Override
    public BigInteger evaluate(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        BigInteger a = (BigInteger) args[0];
        return a.negate();
    }

    @Override
    public String toString() {
        return "-";
    }

    @Override
    public Class getReturnClass() {
        return BigInteger.class;
    }

}
