package vvhile.basic;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import vvhile.basic.operators.Operator;
import vvhile.basic.hoare.BooleanFormula;

/**
 *
 * @author markus
 */
public interface Expression {

    public static final String SORT_BOOLEAN = Boolean.class.getSimpleName();
    public static final String SORT_INTEGER = BigInteger.class.getSimpleName();
    public static final String SORT_SET = java.util.Set.class.getSimpleName();
    public static final String SORT_LIST = java.util.List.class.getSimpleName();
    public static final String SORT_UNKNOWN = "Unknown";

    public Object getValue(State state);

    public Expression subtitute(Expression expression, Variable variable);

    public Expression trySubtitute(Expression expression, Expression subExpression);

    public String getSort();

    public Expression setSort(String newSort);

    public java.util.Set<Variable> freeVariables();

    public java.util.Set<Constant> constants();

    public String toString(boolean latex);

    public static class Constant implements Expression {

        public static boolean showSorts = true;

        private final String sort;
        private final Object value;

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

    }

    public static class Variable implements Expression {

        public static boolean showSorts = true;

        private final String sort;
        private final String name;
        private final String index;

        public Variable(String sort, String name) {
            this(sort, name, null);
        }

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

        public String getName() {
            return name;
        }

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
            hash = 67 * hash + Objects.hashCode(this.sort);
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
            if (!Objects.equals(this.sort, other.sort)) {
                return false;
            }
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

        public Variable dropIndex() {
            return new Variable(sort, name);
        }

    }

    public static class Function implements Expression {

        private final String[] argSorts;
        private final String sort;
        private final Expression[] args;
        private final Operator interpretation;
        private final boolean infix;
        private final boolean parentheses;

        public Function(String[] argSorts, String sort, Expression[] args, Operator interpretation, boolean infix, boolean parentheses) {
            if (args.length != argSorts.length) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            }
            for (int i = 0; i < args.length; i++) {
                if (!(args[i].getSort().equals(argSorts[i]) || "Object".equals(argSorts[i]))) {
                    if ("Object".equals(args[i].getSort()) || Expression.SORT_UNKNOWN.equals(args[i].getSort())) {
                        args[i].setSort(argSorts[i]);
                    } else {
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

        public Expression[] getArgs() {
            return args;
        }

        public Operator getInterpretation() {
            return interpretation;
        }

        @Override
        public Object getValue(State state) {
            Object[] argValues = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                argValues[i] = args[i].getValue(state);
                if (argValues[i] == null) {
                    throw new IllegalArgumentException("One of the arguments has no value.");
                }
            }
            return interpretation.evaluate(argValues);
        }

        @Override
        public Expression subtitute(Expression expression, Variable variable) {
            Expression[] newArgs = new Expression[args.length];
            for (int i = 0; i < args.length; i++) {
                newArgs[i] = args[i].subtitute(expression, variable);
            }
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
    }

    public static class Quantifier implements BooleanFormula {

        private final boolean forAll;
        private final Variable variable;
        private final Expression argument;

        public Quantifier(boolean forAll, Variable variable, Expression argument) {
            this.forAll = forAll;
            this.variable = variable;
            this.argument = argument;
        }

        @Override
        public Object getValue(State state) {
            if (!argument.freeVariables().contains(variable)) {
                return argument.getValue(state);
            }
            return null;
        }

        @Override
        public Expression subtitute(Expression expression, Variable variable) {
            Variable newVar = new Variable(this.variable.getSort(), this.variable.getName() + "'");
            return new Quantifier(forAll, newVar, argument.subtitute(newVar, this.variable).subtitute(expression, variable));
        }

        @Override
        public String getSort() {
            return SORT_BOOLEAN;
        }

        @Override
        public Expression setSort(String newSort) {
            throw new UnsupportedOperationException("Sort of a quantifier is always " + SORT_BOOLEAN + ".");
        }

        @Override
        public java.util.Set<Variable> freeVariables() {
            java.util.Set<Variable> variables = argument.freeVariables();
            variables.remove(variable);
            return variables;
        }

        @Override
        public java.util.Set<Constant> constants() {
            return argument.constants();
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public String toString(boolean latex) {
            return (forAll
                    ? (latex ? "(\\forall" : "(∀")
                    : (latex ? "(\\exists" : "(∃"))
                    + variable + "." + argument + ")";
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.forAll ? 1 : 0);
            hash = 89 * hash + Objects.hashCode(this.variable);
            hash = 89 * hash + Objects.hashCode(this.argument);
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
            final Quantifier other = (Quantifier) obj;
            if (this.forAll != other.forAll) {
                return false;
            }
            if (!Objects.equals(this.variable, other.variable)) {
                return false;
            }
            return Objects.equals(this.argument, other.argument);
        }

        @Override
        public Expression trySubtitute(Expression expression, Expression subExpression) {
            if (subExpression.freeVariables().contains(variable)) {
                return null;
            }
            Expression sub = argument.trySubtitute(expression, subExpression);
            return sub == null ? null : new Quantifier(forAll, variable, sub);
        }

    }

}
