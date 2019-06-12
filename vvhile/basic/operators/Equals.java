package vvhile.basic.operators;

/**
 *
 * @author markus
 */
public class Equals extends Operator<Boolean> {

    private final Class argClass;

    public Equals(Class argClass) {
        this.argClass = argClass;
    }

    @Override
    public Class[] getArgClasses() {
        return new Class[]{argClass, argClass};
    }

    @Override
    public Boolean evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        if (argClass.isInstance(args[0]) && argClass.isInstance(args[1])) {
            return args[0].equals(args[1]);
        } else {
            throw new IllegalArgumentException("Wrong type of arguments.");
        }
    }

    @Override
    public String toString() {
        return "=";
    }

    @Override
    public Class getReturnClass() {
        return Boolean.class;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }

}
