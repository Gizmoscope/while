package vvhile.hoare;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import vvhile.basic.language.BasicOperators;
import vvhile.intrep.Expression;
import vvhile.intrep.State;
import vvhile.intrep.Operator;

/**
 * A boolean formula is an expression of sort boolean.
 *
 * @author markus
 */
public interface BooleanFormula extends Expression, HoareOrBoolean {

    /**
     * @return Must be Expression.SORT_BOOLEAN
     */
    @Override
    public String getSort();

    @Override
    public BooleanFormula subtitute(Expression expression, Variable variable);

    /**
     * Conjunction of several expressions.
     *
     * @param expressions array of boolean formulars
     * @return logical conjunction of the given expressions
     */
    public static BooleanFormula and(BooleanFormula... expressions) {
        return (BooleanFormula) BasicOperators.AND.create(expressions, true);
    }

    /**
     * Disjunction of several expressions.
     *
     * @param expressions array of boolean formulars
     * @return logical disjunction of the given expressions
     */
    public static BooleanFormula or(BooleanFormula... expressions) {
        return (BooleanFormula) BasicOperators.OR.create(expressions, true);
    }

    /**
     * Negation of an expression.
     *
     * @param expression a boolean formular
     * @return logical negation of the given expression
     */
    public static BooleanFormula not(BooleanFormula expression) {
        return (BooleanFormula) BasicOperators.NOT.create(new Expression[]{expression}, true);
    }

    /**
     * Implication of two expressions.
     *
     * @param a the antecedent, a boolean formular
     * @param b the consequent, a boolean formular
     * @return logical implication "a implies b"
     */
    public static BooleanFormula implies(BooleanFormula a, BooleanFormula b) {
        return (BooleanFormula) BasicOperators.IMPLIES.create(new Expression[]{a, b}, true);
    }

    /**
     * A boolean variable is a variable of type boolean.
     */
    public static class BooleanVariable extends Expression.Variable implements BooleanFormula {

        /**
         * Create a new boolean variable.
         *
         * @param name name of the variable
         */
        public BooleanVariable(String name) {
            super(Expression.SORT_BOOLEAN, name);
        }

        /**
         * Create a new boolean variable with an index.
         *
         * @param name name of the variable
         * @param index index of the variable
         */
        public BooleanVariable(String name, String index) {
            super(Expression.SORT_BOOLEAN, name, index);
        }

        /**
         * If the variable that is to be substituted agrees with this object,
         * then the given expression is returned. In that case the expression has
         * to be a boolean formular.
         * 
         * @param expression an expression
         * @param variable a variable
         * @return the result of substituting the expression for the variable
         */
        @Override
        public BooleanFormula subtitute(Expression expression, Variable variable) {
            if (equals(variable)) {
                if (expression instanceof BooleanFormula) {
                    return (BooleanFormula) expression;
                } else {
                    throw new IllegalArgumentException("The expression has to be a boolean formular.");
                }
            } else {
                return this;
            }
        }

    }

    /**
     * A boolean constant is a constant of type boolean.
     */
    public static class BooleanConstant extends Expression.Constant implements BooleanFormula {

        /**
         * Create a new boolean constant.
         *
         * @param value value of the constant
         */
        public BooleanConstant(Boolean value) {
            super(Expression.SORT_BOOLEAN, value);
        }

        @Override
        public BooleanFormula subtitute(Expression expression, Variable variable) {
            return this;
        }

    }

    /**
     * A boolean function is a function of return type boolean.
     */
    public static class BooleanFunction extends Expression.Function implements BooleanFormula {

        /**
         * Create a new boolean function.
         *
         * @param argSorts sorts of the arguments
         * @param args expressions plugged in a arguments
         * @param interpretation the operaton represented by this function
         * @param infix is this an infix function
         * @param parentheses result should show parantheses
         */
        public BooleanFunction(String[] argSorts, Expression[] args, Operator interpretation, boolean infix, boolean parentheses) {
            super(argSorts, Expression.SORT_BOOLEAN, args, interpretation, infix, parentheses);
        }

        @Override
        public BooleanFormula subtitute(Expression expression, Variable variable) {
            return (BooleanFormula) super.subtitute(expression, variable);
        }

    }

    /**
     * A black box represents a boolean formular of unknown content. An object
     * of this class remembers all substitutions applied to it. A black box can
     * be replaced (aka filled) by an actual boolean formular. All substitutions
     * are then applied to it.
     */
    public static class BlackBox implements BooleanFormula {

        private final String name;
        private final java.util.List<Substitution> substitutions;

        /**
         * Create a new black box.
         *
         * @param name name of the blackbox
         */
        public BlackBox(String name) {
            this.name = name;
            this.substitutions = new LinkedList<>();
        }

        /**
         * @return name of the blackbox
         */
        public String getName() {
            return name;
        }

        @Override
        public String getSort() {
            return Expression.SORT_BOOLEAN;
        }

        /**
         * The value of a blackbox cannot be determined. This method always
         * returns null
         * 
         * @param state a state
         * @return null
         */
        @Override
        public Boolean getValue(State state) {
            return null;
        }

        /**
         * As a black box is not an actual expression all substitutions applied
         * to it are stored in an internal list.
         *
         * @param expression an expression replacing the variable
         * @param variable a variable
         * @return another blackbox that remembers the substitution
         */
        @Override
        public BlackBox subtitute(Expression expression, Variable variable) {
            BlackBox subtituted = new BlackBox(name);
            subtituted.substitutions.addAll(substitutions);
            subtituted.substitutions.add(new Substitution(variable, expression));
            return subtituted;
        }

        @Override
        public Expression setSort(String newSort) {
            throw new UnsupportedOperationException("The sort of a boolean formular cannot be changed.");
        }

        @Override
        public java.util.Set<Variable> freeVariables() {
            return Collections.EMPTY_SET;
        }

        @Override
        public java.util.Set<Constant> constants() {
            return Collections.EMPTY_SET;
        }

        /**
         * Replaces the blackbox by an actual expression and applies all
         * substitutions that were applied to the blackbox.
         * 
         * @param expression a boolean formular replacing the blackbox
         * @return the given expression with all substitutions of the blackbox applied to it
         */
        public BooleanFormula fill(BooleanFormula expression) {
            BooleanFormula substituted = expression;
            // apply all substitutions
            for (Substitution s : substitutions) {
                substituted = substituted.subtitute(s.expression, s.variable);
            }
            return substituted;
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public String toString(boolean latex) {
            StringBuilder builder = new StringBuilder(name);
            substitutions.stream().forEach((s) -> {
                builder.append(s.toString(latex));
            });
            return builder.toString();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.name);
            hash = 17 * hash + Objects.hashCode(this.substitutions);
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
            final BlackBox other = (BlackBox) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return Objects.equals(this.substitutions, other.substitutions);
        }

        @Override
        public Expression trySubtitute(Expression expression, Expression subExpression) {
            return null;
        }

        /*
         * An instance of this class remembers the data of an substitution. This
         * is merely a data class.
         */
        private class Substitution {

            private final Variable variable;
            private final Expression expression;

            public Substitution(Variable variable, Expression expression) {
                this.variable = variable;
                this.expression = expression;
            }

            @Override
            public String toString() {
                return toString(false);
            }

            private String toString(boolean latex) {
                return "[" + variable.toString(latex) + '/' + expression.toString(latex) + ']';
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 37 * hash + Objects.hashCode(this.variable);
                hash = 37 * hash + Objects.hashCode(this.expression);
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
                final Substitution other = (Substitution) obj;
                if (!Objects.equals(this.variable, other.variable)) {
                    return false;
                }
                return Objects.equals(this.expression, other.expression);
            }
        }

    }
    
    /**
     * A quantifier represents either an "for all" or an "exits" expression. 
     */
    public static class Quantifier implements BooleanFormula {

        // true: "for all", false: "exists"
        private final boolean forAll;
        private final Variable variable;
        private final Expression argument;

        /**
         * Create a new quantified expression.
         * 
         * @param forAll true is all quantified, false if existence expression
         * @param variable quantified variable
         * @param argument quantified expression
         */
        public Quantifier(boolean forAll, Variable variable, Expression argument) {
            this.forAll = forAll;
            this.variable = variable;
            this.argument = argument;
        }

        /**
         * Usually a quantified expression cannot simply be evaluated. If the
         * quantified variable does not appear in the quantified expression, then
         * this method evaluates that expression. Otherwise it returns null.
         * 
         * @param state a state
         * @return the value of the quantified expression in the given state if 
         * it does not contain the quantified varialbe and null otherwise
         */
        @Override
        public Object getValue(State state) {
            if (!argument.freeVariables().contains(variable)) {
                return argument.getValue(state);
            }
            return null;
        }

        @Override
        public BooleanFormula subtitute(Expression expression, Variable variable) {
            // One has to be careful not to substitute the quantified variable in
            // the quantified expression, so introduce a new variable.
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
            // by definition the quantified variable is not free, remove it from
            // the set of free variables of the quantified expression
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
