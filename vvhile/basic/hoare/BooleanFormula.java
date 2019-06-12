package vvhile.basic.hoare;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import vvhile.basic.Expression;
import vvhile.basic.State;
import vvhile.basic.operators.Operator;

/**
 *
 * @author markus
 */
public interface BooleanFormula extends Expression, HoareOrBoolean {

    /**
     * @return Must be Expression.SORT_BOOLEAN
     */
    @Override
    public String getSort();

    public static class BooleanVariable extends Expression.Variable implements BooleanFormula {

        public BooleanVariable(String name) {
            super(Expression.SORT_BOOLEAN, name);
        }

        public BooleanVariable(String name, String index) {
            super(Expression.SORT_BOOLEAN, name, index);
        }

    }

    public static class BooleanConstant extends Expression.Constant implements BooleanFormula {

        public BooleanConstant(Boolean value) {
            super(Expression.SORT_BOOLEAN, value);
        }

    }

    public static class BooleanFunction extends Expression.Function implements BooleanFormula {

        public BooleanFunction(String[] argSorts, Expression[] args, Operator interpretation, boolean infix, boolean parentheses) {
            super(argSorts, Expression.SORT_BOOLEAN, args, interpretation, infix, parentheses);
        }

    }

    public static class BlackBox implements BooleanFormula {

        private final String name;
        private final java.util.List<Substitution> substitutions;

        public BlackBox(String name) {
            this.name = name;
            this.substitutions = new LinkedList<>();
        }

        public String getName() {
            return name;
        }

        @Override
        public String getSort() {
            return Expression.SORT_BOOLEAN;
        }

        @Override
        public Boolean getValue(State state) {
            return null;
        }

        @Override
        public Expression subtitute(Expression expression, Variable variable) {
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

        public Expression fill(Expression expression) {
            Expression substituted = expression;
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
}
