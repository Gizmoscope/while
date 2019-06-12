package vvhile.basic.operators;

import java.math.BigInteger;

/**
 *
 * @author markus
 */
public class LessEqual extends Operator<Boolean> {

    public static final LessEqual LESS_EQUAL = new LessEqual();

    private LessEqual() {
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{BigInteger.class, BigInteger.class};
    }

    @Override
    public Boolean evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        BigInteger a = (BigInteger) args[0];
        BigInteger b = (BigInteger) args[1];
        return a.compareTo(b) <= 0;
    }

    @Override
    public String toString() {
        return "≤";
    }

    @Override
    public Class getReturnClass() {
        return Boolean.class;
    }

}
