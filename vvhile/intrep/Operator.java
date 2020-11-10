package vvhile.intrep;

import java.util.function.BiFunction;
import java.util.function.Function;
import vvhile.hoare.BooleanFormula;

/**
 * An object of the Operator class represents a mathematical operator that can be
 * used in a program. An operator is therefore a function that assigns to a list
 * of parameter a value. Each of the arguments and the operator value have a type.
 * 
 * @author markus
 * @param <T> the return type
 */
public abstract class Operator<T> {

    /**
     * @return Array of types, here classes, of the arguments
     */
    public abstract Class[] getArgClasses();

    /**
     * @return type, here class, of the return value
     */
    public abstract Class getReturnClass();

    /**
     * Apply the operator to a list of arguments. This method should only be
     * applied if th arguments are of the correct types (classes).
     * 
     * @param args Array of arguments
     * @return the operator value when applied to the arguments
     */
    public abstract T evaluate(Object... args);

    /**
     * If an operator is feed with expressions the result is a new expression of
     * function type. This methods creates this function expression corresponding
     * to this operator for the given list of argument expressions.
     * 
     * @param args Array of expressions representing arguments to this operator
     * @param parentheses true if the resulting expression should have parentheses
     * around it in string representations
     * @return the expression resulting when applying the operator to the arguments
     */
    public Expression.Function create(Expression[] args, boolean parentheses) {
        // determine the sorts of the arguments (names of the argument classes)
        String[] argSorts = new String[getArgClasses().length];
        for (int i = 0; i < argSorts.length; i++) {
            argSorts[i] = getArgClasses()[i].getSimpleName();
        }
        // If the operator returns booleans then the resulting expression is a 
        // boolean function
        if (Expression.SORT_BOOLEAN.equals(getReturnClass().getSimpleName())) {
            return new BooleanFormula.BooleanFunction(
                    argSorts,
                    args, this, true,
                    parentheses
            );
        } else {
            return new Expression.Function(
                    argSorts,
                    getReturnClass().getSimpleName(),
                    args, this, true,
                    parentheses
            );
        }
    }

    /**
     * Returns a string representation of the operator.
     * @param latex true if the string should be in a LaTeX format
     * @return string representation of the operator
     */
    public String toString(boolean latex) {
        String s = toString();
        // For the following cases there are LaTeX codes
        if (latex) {
            switch (s) {
                case "∧":
                    return "\\wedge ";
                case "≥":
                    return "\\geq ";
                case "⟸":
                    return "\\Leftarrow ";
                case "⟹":
                    return "\\Rightarrow ";
                case "≤":
                    return "\\leq ";
                case "¬":
                    return "\\neg ";
                case "∨":
                    return "\\vee ";
                case "∪":
                    return "\\cup ";
                case "∩":
                    return "\\cap ";
                case "−":
                    return "-";
                case "∊":
                    return "\\in ";
                case "⋅":
                    return "\\cdot ";
                default:
                    return s;
            }
        } else {
            return s;
        }
    }
    
    // From here on there are only static creation methods.

    /**
     * Turns a bifunction into a binary operator, ie an operator with two
     * arguments.
     * 
     * @param <A> Type of first argument
     * @param <B> Type of second argument
     * @param <C> Type of return value
     * @param A Class of first argument
     * @param B Class of second argument
     * @param C Class of return value
     * @param f a bifunction
     * @param symbol string representation of the operator symbol
     * @return binary operator representing the bifunction
     */
    public static <A, B, C> Operator<C> createBinaryOperator(Class A, Class B, Class C, BiFunction<A, B, C> f, String symbol) {
        return new Operator<C>() {
            @Override
            public Class[] getArgClasses() {
                return new Class[]{A, B};
            }

            @Override
            public Class getReturnClass() {
                return C;
            }

            @Override
            public C evaluate(Object... args) {
                // Check the arguments
                if (args.length != 2) {
                    throw new IllegalArgumentException("Wrong number of arguments.");
                }
                if (args[0] == null || args[1] == null) {
                    return null;
                }
                // apply the bifunction to the arguments
                A a = (A) args[0];
                B b = (B) args[1];
                return f.apply(a, b);
            }

            @Override
            public String toString() {
                return symbol;
            }
        };
    }

    /**
     * Turns a function into a unary operator, ie an operator with one argument.
     * 
     * @param <A> Type of the argument
     * @param <B> Type of return value
     * @param A Class of the argument
     * @param B Class of return value
     * @param f a function
     * @param symbol string representation of the operator symbol
     * @return unary operator representing the function
     */
    public static <A, B> Operator<B> createUnaryOperator(Class A, Class B, Function<A, B> f, String symbol) {
        return new Operator<B>() {
            @Override
            public Class[] getArgClasses() {
                return new Class[]{A};
            }

            @Override
            public Class getReturnClass() {
                return B;
            }

            @Override
            public B evaluate(Object... args) {
                // Check the arguments
                if (args.length != 1) {
                    throw new IllegalArgumentException("Wrong number of arguments.");
                }
                if (args[0] == null) {
                    return null;
                }
                // apply the function to the argument
                A a = (A) args[0];
                return f.apply(a);
            }

            @Override
            public String toString() {
                return symbol;
            }
        };
    }

}
