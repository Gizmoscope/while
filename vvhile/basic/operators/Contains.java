package vvhile.basic.operators;

import java.util.Set;

/**
 *
 * @author markus
 */
public class Contains extends Operator<Boolean> {

    public static final Contains IN = new Contains();

    private Contains() {
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{Object.class, Set.class};
    }

    @Override
    public Boolean evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        Object a = args[0];
        Set set = (Set) args[1];
        return set.contains(a);
    }

    @Override
    public String toString() {
        return "∊";
    }

    @Override
    public Class getReturnClass() {
        return Boolean.class;
    }

}
