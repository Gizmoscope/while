package vvhile.basic.operators;

import vvhile.basic.Expression;
import vvhile.basic.hoare.BooleanFormula;

/**
 *
 * @author markus
 * @param <T> Return Type
 */
public abstract class Operator<T> {

    public abstract Class[] getArgClasses();

    public abstract Class getReturnClass();

    public abstract T evaluate(Object... args);

    public Expression.Function create(Expression[] args, boolean parentheses) {
        String[] argSorts = new String[getArgClasses().length];
        for (int i = 0; i < argSorts.length; i++) {
            argSorts[i] = getArgClasses()[i].getSimpleName();
        }
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

    public static BooleanFormula and(Expression... expressions) {
        return (BooleanFormula) And.AND.create(expressions, true);
    }

    public static BooleanFormula or(Expression... expressions) {
        return (BooleanFormula) Or.OR.create(expressions, true);
    }

    public static BooleanFormula not(Expression expression) {
        return (BooleanFormula) Not.NOT.create(new Expression[]{expression}, true);
    }

    public static BooleanFormula implies(Expression a, Expression b) {
        return (BooleanFormula) Implies.IMPLIES.create(new Expression[]{a, b}, true);
    }

    public String toString(boolean latex) {
        String s = toString();
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

}
