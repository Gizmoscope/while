package vvhile.intrep;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import vvhile.hoare.BooleanFormula;

/**
 * An expression is a formular of a given type, called sort, that can be
 * evaluated in a given state. An expression contains variables, whose value is
 * determined by a state, and operations, called functions, that combine the
 * values of variables and constants.
 *
 * @author markus
 */
public interface Expression extends ASTElement {

    /**
     * The boolean sort. Variables can take the values <code> true </code> and
     * <code> false </code>.
     */
    public static final String SORT_BOOLEAN = Boolean.class.getSimpleName();

    /**
     * The integer sort. Variables can take any integer as a value.
     */
    public static final String SORT_INTEGER = BigInteger.class.getSimpleName();

    /**
     * The string sort. Variables can take any string as a value.
     */
    public static final String SORT_STRING = String.class.getSimpleName();

    /**
     * The set sort. Variables can take any set as a value.
     */
    public static final String SORT_SET = java.util.Set.class.getSimpleName();

    /**
     * The list sort. Variables can take any list as a value.
     */
    public static final String SORT_LIST = java.util.List.class.getSimpleName();

    /**
     * The so-called unknown sort. The sort of a variable with this sort is not
     * yet determined and can still be changed.
     */
    public static final String SORT_UNKNOWN = "Unknown";

    /**
     * Determines the value of the expression in the given state.
     *
     * @param state a state
     * @return the value of the expression in the given state.
     */
    public Object getValue(State state);

    /**
     * Replaces every occurrence of the given variable by the given expression.
     *
     * @param expression an expression
     * @param variable a variable
     * @return the expression after every occurrence of the variable is replaced
     * by the expression.
     */
    public Expression subtitute(Expression expression, Variable variable);

    /**
     * Tries to replace a subExpression of this expression by the given
     * expression. This is a useful method for theorem provers.
     *
     * @param expression an expression
     * @param subExpression an expression that is propably a subexpression of
     * this expression
     * @return a substituted expression if successful or null otherwise
     */
    public Expression trySubtitute(Expression expression, Expression subExpression);

    /**
     * @return the sort of this variable. It might be unknown.
     */
    public String getSort();

    /**
     * Gives the expression a sort. This must only happen if the sort was
     * unknown before.
     *
     * @param newSort a sort
     * @return this expression but with an assigned sort
     */
    public Expression setSort(String newSort);

    /**
     * @return set of all free variables appearing in the expression.
     */
    public java.util.Set<Variable> freeVariables();

    /**
     * @return set of all constants appearing in the expression.
     */
    public java.util.Set<Constant> constants();

    /**
     * @param latex true if the return string should represent LaTeX code
     * @return a string representation
     */
    public String toString(boolean latex);

    /**
     * Replaces every occurrence of the given blackbox by the given boolean formular.
     * 
     * @param blackBox a blackbox
     * @param substitution a boolean formular
     * @return the expression after every occurrence of the blackbox is replaced
     * by the boolean formular.
     */
    public Expression fillBlackBox(BooleanFormula.BlackBox blackBox, BooleanFormula substitution);

    /**
     * A constant is an expression that evaluates to the same value in any
     * state.
     */
    public static class Constant implements Expression {

        public static boolean showSorts = true;

        private final String sort;
        private final Object value;

        /**
         * Creates a new constant of the given value and sort.
         *
         * @param sort a sort
         * @param value a value of that sort
         */
        public Constant(String sort, Object value) {
            this.sort = sort;
            this.value = value;
        }

        @Override
        public String getSort() {
            return sort;
        }

        @Override
        public Object getValue(State state) {
            return value;
        }

        @Override
        public Expression subtitute(Expression expression, Variable variable) {
            return this;
        }

        @Override
        public String toString(boolean latex) {
            String string = value == null ? (latex ? "\\sim" : "~") : value.toString();
            return showSorts ? string + "::" + sort : string;
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public Expression setSort(String newSort) {
            throw new UnsupportedOperationException("Sort of constants can't be changed.");
        }

        @Override
        public java.util.Set<Variable> freeVariables() {
            return Collections.EMPTY_SET;
        }

        @Override
        public java.util.Set<Constant> constants() {
            java.util.Set<Constant> constants = new HashSet<>();
            constants.add(this);
            return constants;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.sort);
            hash = 97 * hash + Objects.hashCode(this.value);
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Constant other = (Constant) obj;
            if (!Objects.equals(this.sort, other.sort)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

        @Override
        public Expression trySubtitute(Expression expression, Expression subExpression) {
            return null;
        }

        @Override
        public Expression fillBlackBox(BooleanFormula.BlackBox blackBox, BooleanFormula substitution) {
            return this;
        }

    }

    /**
     * A variable is an expression with a given name whose value depends
     * completely on the state its evaluated in.
     */
    public static class Variable implements Expression {

        public static boolean showSorts = true;

        private final String sort;
        private final String name;
        private final String index;

        /**
         * Creates a new variable of the given name and sort.
         *
         * @param sort a sort
         * @param name a name
         */
        public Variable(String sort, String name) {
            this(sort, name, null);
        }

        /**
         * Creates a new variable of the given name, index and sort. The index
         * can be used to have more flexibility in the naming of variables.
         *
         * @param sort a sort
         * @param name a name
         * @param index an index
         */
        public Variable(String sort, String name, String index) {
            this.sort = sort;
            this.name = name;
            this.index = index;
        }

        @Override
        public String getSort() {
            return sort;
        }

        @Override
        public Object getValue(State state) {
            return state.getValueFor(this);
        }

        @Override
        public Expression subtitute(Expression expression, Variable variable) {
            return equals(variable) ? expression : this;
        }

        /**
         * @return the name of the variable
         */
        public String getName() {
            return name;
        }

        /**
         * @return the index of the variable
         */
        public String getIndex() {
            return index;
        }

        @Override
        public String toString(boolean latex) {
            if (showSorts) {
                return name + (index != null ? "_" + (latex ? "{" + index + "}" : index) : "") + "::" + sort;
            } else {
                return name + (index != null ? "_" + (latex ? "{" + index + "}" : index) : "");
            }
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public Expression setSort(String newSort) {
            if (SORT_UNKNOWN.equals(sort) || "Object".equals(sort)) {
                if (SORT_BOOLEAN.equals(newSort)) {
                    return new BooleanFormula.BooleanVariable(name, index);
                } else {
                    return new Variable(newSort, name, index);
                }
            } else {
                throw new IllegalStateException("Sort is already known and must not be changed.");
            }
        }

        @Override
        public java.util.Set<Variable> freeVariables() {
            java.util.Set<Variable> fv = new HashSet<>();
            fv.add(this);
            return fv;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.name);
            hash = 67 * hash + Objects.hashCode(this.index);
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Variable other = (Variable) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return Objects.equals(this.index, other.index);
        }

        @Override
        public java.util.Set<Constant> constants() {
            return Collections.EMPTY_SET;
        }

        @Override
        public Expression trySubtitute(Expression expression, Expression subExpression) {
            return equals(subExpression) ? expression : null;
        }

        /**
         * @return the variable but without the index
         */
        public Variable dropIndex() {
            return new Variable(sort, name);
        }

        @Override
        public Expression fillBlackBox(BooleanFormula.BlackBox blackBox, BooleanFormula substitution) {
            return this;
        }

    }

    /**
     * A function is an expression that combines several expressions into a new
     * one. A function always has an operator that serves as an interpretation
     * for the function.
     */
    public static class Function implements Expression {

        private final String[] argSorts;
        private final String sort;
        private final Expression[] args;
        private final Operator interpretation;
        private final boolean infix;
        private final boolean parentheses;

        /**
         * Creates a new function from the given data. It is important that the
         * number of sorts for the arguments and the number of expressions for
         * the arguments agree. Of course the interpretation, as an operator,
         * should also have the same number of arguments and the sorts have to
         * be compatible.
         *
         * @param argSorts a list of sorts, the sorts of the arguments
         * @param sort the sort of the resulting expression
         * @param args a list of expressions, the arguments
         * @param interpretation an operation used for the interpretation, aka
         * evaluation
         * @param infix should this function be parsed useing infix notation
         * @param parentheses should the string representation of this
         * expression be enclosed with parentheses
         */
        public Function(String[] argSorts, String sort, Expression[] args, Operator interpretation, boolean infix, boolean parentheses) {
            if (args.length != argSorts.length) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            }
            for (int i = 0; i < args.length; i++) {
                if (!(args[i].getSort().equals(argSorts[i]) || "Object".equals(argSorts[i]))) {
                    // Using this expression as the contex, the sort of the
                    // argument can be determined
                    if ("Object".equals(args[i].getSort()) || Expression.SORT_UNKNOWN.equals(args[i].getSort())) {
                        // Give the argument the correct sort
                        args[i].setSort(argSorts[i]);
                    } else {
                        // The sort didn't match the already existing sort
                        throw new IllegalArgumentException("Wrong sort.");
                    }
                }
            }
            if (infix && (args.length != 1 && args.length != 2)) {
                throw new IllegalArgumentException("Infix notation is only possible for one or two arguments.");
            }
            this.argSorts = argSorts;
            this.sort = sort;
            this.args = args;
            this.interpretation = interpretation;
            this.infix = infix;
            this.parentheses = parentheses;
        }

        /**
         * @return the list of argument expressions of this function
         */
        public Expression[] getArgs() {
            return args;
        }

        /**
         * @return the operator used for interpreting/evaluating this expression
         */
        public Operator getInterpretation() {
            return interpretation;
        }

        @Override
        public Object getValue(State state) {
            Object[] argValues = new Object[args.length];
            // Determine the value of all arguments in the given state...
            for (int i = 0; i < args.length; i++) {
                argValues[i] = args[i].getValue(state);
                if (argValues[i] == null) {
                    throw new IllegalArgumentException("One of the arguments has no value.");
                }
            }
            // ...and use them as arguments for the interpretation operator
            return interpretation.evaluate(argValues);
        }

        @Override
        public Expression subtitute(Expression expression, Variable variable) {
            Expression[] newArgs = new Expression[args.length];
            // the substitution has to be done for every argument
            for (int i = 0; i < args.length; i++) {
                newArgs[i] = args[i].subtitute(expression, variable);
            }
            // If this is a boolean formular the result should again be a boolean formular.
            if (Expression.SORT_BOOLEAN.equals(sort)) {
                return new BooleanFormula.BooleanFunction(argSorts, newArgs, interpretation, infix, parentheses);
            } else {
                return new Function(argSorts, sort, newArgs, interpretation, infix, parentheses);
            }
        }

        @Override
        public String getSort() {
            return sort;
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public String toString(boolean latex) {
            if (infix && args.length == 1) {
                if (parentheses) {
                    return "(" + interpretation.toString(latex) + args[0].toString(latex) + ")";
                } else {
                    return interpretation.toString(latex) + args[0].toString(latex);
                }
            } else if (infix && args.length == 2) {
                if (parentheses) {
                    return "(" + args[0].toString(latex) + " " + interpretation.toString(latex) + " " + args[1].toString(latex) + ")";
                } else {
                    return args[0].toString(latex) + " " + interpretation.toString(latex) + " " + args[1].toString(latex);
                }
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append(interpretation.toString(latex)).append('(');
                if (args.length > 0) {
                    builder.append(args[0].toString(latex));
                }
                for (int i = 1; i < args.length; i++) {
                    builder.append(", ").append(args[i].toString(latex));
                }
                return builder.append(')').toString();
            }
        }

        @Override
        public Expression setSort(String newSort) {
            if (SORT_UNKNOWN.equals(sort)) {
                if (SORT_BOOLEAN.equals(newSort)) {
                    return new BooleanFormula.BooleanFunction(argSorts, args, interpretation, infix, parentheses);
                } else {
                    return new Function(argSorts, newSort, args, interpretation, infix, parentheses);
                }
            } else {
                throw new IllegalStateException("Sort is already known and must not be changed.");
            }
        }

        @Override
        public java.util.Set<Variable> freeVariables() {
            java.util.Set<Variable> fv = new HashSet<>();
            for (Expression expression : args) {
                fv.addAll(expression.freeVariables());
            }
            return fv;
        }

        @Override
        public java.util.Set<Constant> constants() {
            java.util.Set<Constant> constants = new HashSet<>();
            for (Expression expression : args) {
                constants.addAll(expression.constants());
            }
            return constants;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Arrays.deepHashCode(this.args);
            hash = 97 * hash + Objects.hashCode(this.interpretation);
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Function other = (Function) obj;
            if (!Arrays.deepEquals(this.args, other.args)) {
                return false;
            }
            return Objects.equals(this.interpretation, other.interpretation);
        }

        @Override
        public Expression trySubtitute(Expression expression, Expression subExpression) {
            if (this.equals(subExpression)) {
                return expression;
            } else {
                Expression[] subs = new Expression[args.length];
                boolean found = false;
                for (int i = 0; i < args.length; i++) {
                    Expression sub = args[i].trySubtitute(expression, subExpression);
                    if (sub == null) {
                        subs[i] = args[i];
                    } else {
                        subs[i] = sub;
                        found = true;
                    }
                }
                if (found) {
                    if (Expression.SORT_BOOLEAN.equals(sort)) {
                        return new BooleanFormula.BooleanFunction(argSorts, subs, interpretation, infix, parentheses);
                    }
                    return new Function(argSorts, sort, subs, interpretation, infix, parentheses);
                } else {
                    return null;
                }
            }
        }

        @Override
        public Expression fillBlackBox(BooleanFormula.BlackBox blackBox, BooleanFormula substitution) {
            Expression[] newArgs = new Expression[args.length];
            // the substitution has to be done for every argument
            for (int i = 0; i < args.length; i++) {
                newArgs[i] = args[i].fillBlackBox(blackBox, substitution);
            }
            // If this is a boolean formular the result should again be a boolean formular.
            if (Expression.SORT_BOOLEAN.equals(sort)) {
                return new BooleanFormula.BooleanFunction(argSorts, newArgs, interpretation, infix, parentheses);
            } else {
                return new Function(argSorts, sort, newArgs, interpretation, infix, parentheses);
            }
        }
    }

}
