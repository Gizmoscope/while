package vvhile.basic.operators;

/**
 *
 * @author markus
 */
public class ImpliedBy extends Operator<Boolean> {

    public static final ImpliedBy IMPLIED_BY = new ImpliedBy();

    private ImpliedBy() {
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{Boolean.class, Boolean.class};
    }

    @Override
    public Boolean evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        if (args[0] == null || args[1] == null) {
            return null;
        }
        Boolean a = (Boolean) args[0];
        Boolean b = (Boolean) args[1];
        return a || !b;
    }

    @Override
    public String toString() {
        return "⟸";
    }

    @Override
    public Class getReturnClass() {
        return Boolean.class;
    }

}
