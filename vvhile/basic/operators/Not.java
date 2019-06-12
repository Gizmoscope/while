package vvhile.basic.operators;

/**
 *
 * @author markus
 */
public class Not extends Operator<Boolean> {
    
    public static final Not NOT = new Not();

    private Not() {
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{Boolean.class};
    }

    @Override
    public Boolean evaluate(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        return args[0] == null ? null : !(Boolean) args[0];
    }

    @Override
    public String toString() {
        return "¬";
    }

    @Override
    public Class getReturnClass() {
        return Boolean.class;
    }

}
