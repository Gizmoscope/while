package vvhile.intrep;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import vvhile.hoare.BooleanFormula;

/**
 * A statement is a piece of a program. Statements can be put together to create
 * new statements. One can run a statement in a given state to obtain the state
 * an a remaining statement that results in running that piece of program. The
 * pair consisting of statement and state is a configuration.
 *
 * @author markus
 */
public interface Statement extends ASTElement {

    /**
     * Executes this statement in the given state.
     *
     * @param state a state
     * @return resulting configuration, ie resulting statement + state
     */
    public Configuration run(State state);

    /**
     * @param latex true if the return string should represent LaTeX code
     * @return a string representation
     */
    public String toString(boolean latex);

    /**
     * @return the set of all variables appearing in this statement
     */
    public Set<Expression.Variable> variables();

    /**
     * @return the set of all variables appearing in this statement that are
     * written to
     */
    public Set<Expression.Variable> writtenVariables();

    /**
     * @return the set of all variables appearing in this statement that are
     * read from
     */
    public Set<Expression.Variable> readVariables();

    /**
     * An assignment is a statement of the form <code> x := a </code> where 
     * <code> x </code> is a variable and <code> a </code> is an expression. If
     * an assignment is executed, then the value of the variable in the given
     * state is substituted by the value of the expression evaluated in that
     * state.
     */
    public static class Assignment implements Statement {

        private final Expression.Variable variable;
        private final Expression expression;

        /**
         * Creates a new assignment given the variable and the expression.
         *
         * @param variable a variable
         * @param expression an expression
         */
        public Assignment(Expression.Variable variable, Expression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        /**
         * The value of the variable in the given state is substituted by the
         * value of the expression evaluated in the state. The resulting
         * configuration contains no statement anymore but only the new state.
         */
        @Override
        public Configuration run(State state) {
            return new Configuration(null,
                    state.substitute(expression.getValue(state), variable));
        }

        @Override
        public String toString() {
            return toString(false);
        }

        /**
         * @return the variable, aka left hand side
         */
        public Expression.Variable getVariable() {
            return variable;
        }

        /**
         * @return the expression, aka right hand side
         */
        public Expression getExpression() {
            return expression;
        }

        @Override
        public String toString(boolean latex) {
            return (latex ? "$" : "") + variable.toString(latex) + " := " + expression.toString(latex) + (latex ? "$" : "");
        }

        @Override
        public Set<Expression.Variable> variables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.add(variable);
            variables.addAll(expression.freeVariables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.add(variable);
            return variables;
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            return expression.freeVariables();
        }

    }

    /**
     * A skip statement is an empty statement that does not do anything. It is
     * useful for instance for empty if or else clauses.
     */
    public static class Skip implements Statement {

        /**
         * Creates a new skip statement.
         */
        public Skip() {
        }

        @Override
        public Configuration run(State state) {
            return new Configuration(null, state);
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public String toString(boolean latex) {
            return latex ? "\\lst{skip}" : "skip";
        }

        @Override
        public Set<Expression.Variable> variables() {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            return Collections.EMPTY_SET;
        }

    }

    /**
     * A composition is a pair of two statements that are meant to be executed
     * one after the other.
     */
    public static class Composition implements Statement {

        private final Statement firstStatement;
        private final Statement secondStatement;

        /**
         * Creates a composition of the given statements
         *
         * @param firstStatement a statement
         * @param secondStatement a statement
         */
        public Composition(Statement firstStatement, Statement secondStatement) {
            this.firstStatement = firstStatement;
            this.secondStatement = secondStatement;
        }

        @Override
        public Configuration run(State state) {
            // Run the first statement
            Configuration newConf = firstStatement.run(state);
            // if the resulting configuration does not contain a resulting
            // statement, the result of this method is given by the new state
            // and the second statement of the composition
            if (newConf.getProgram() == null) {
                return new Configuration(secondStatement, newConf.getState());
            } else {
                // otherwise the result given by the new state and the composition
                // of the resulting statement and the second statement of the
                // composition
                return new Configuration(new Composition(
                        newConf.getProgram(),
                        secondStatement
                ), newConf.getState());
            }
        }

        @Override
        public String toString() {
            return toString(false);
        }

        /**
         * @return the first statement
         */
        public Statement getFirstStatement() {
            return firstStatement;
        }

        /**
         * @return the second statement
         */
        public Statement getSecondStatement() {
            return secondStatement;
        }

        @Override
        public String toString(boolean latex) {
            return firstStatement.toString(latex) + "; " + secondStatement.toString(latex);
        }

        @Override
        public Set<Expression.Variable> variables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(firstStatement.variables());
            variables.addAll(secondStatement.variables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(firstStatement.writtenVariables());
            variables.addAll(secondStatement.writtenVariables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(firstStatement.readVariables());
            variables.addAll(secondStatement.readVariables());
            return variables;
        }

    }

    /**
     * An if-then-else statement is a statement of the form 
     * <code> if(b) { S_1 } else { S_2 } </code> where <code> b </code> is a
     * condition and <code> S_1 </code> and <code> S_2 </code> are the
     * corresponding conditional statements.
     */
    public static class If implements Statement {

        private final BooleanFormula condition;
        private final Statement ifStatement;
        private final Statement elseStatement;

        /**
         * Creates an if-then-else statement given the condition, the if and the
         * else statement.
         *
         * @param condition a boolean formular
         * @param ifStatement a statement to be executed if the condition is met
         * @param elseStatement a statement to be executed if the condition is
         * not met
         */
        public If(BooleanFormula condition, Statement ifStatement, Statement elseStatement) {
            this.condition = condition;
            this.ifStatement = ifStatement;
            this.elseStatement = elseStatement;
        }

        @Override
        public Configuration run(State state) {
            if (condition.getValue(state) == null) {
                throw new RuntimeException("Condition cannot be evaluated.");
            } else if (!(condition.getValue(state) instanceof Boolean)) {
                throw new RuntimeException("Condition is not of sort " + Expression.SORT_BOOLEAN + ".");
            } else if ((Boolean) condition.getValue(state)) {
                return new Configuration(ifStatement, state);
            } else {
                return new Configuration(elseStatement, state);
            }
        }

        /**
         * @return the condition
         */
        public BooleanFormula getCondition() {
            return condition;
        }

        /**
         * @return the statement to be executed if the condition is met
         */
        public Statement getIfStatement() {
            return ifStatement;
        }

        /**
         * @return the statement to be executed if the condition is not met
         */
        public Statement getElseStatement() {
            return elseStatement;
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public String toString(boolean latex) {
            return (latex ? "\\lst{if} ($" : "if (")
                    + condition.toString(latex) + (latex ? "$) \\{" : ") {")
                    + ifStatement.toString(latex) + (latex ? "\\} \\lst{else} \\{" : "} else {")
                    + elseStatement.toString(latex) + (latex ? "\\}" : "}");
        }

        @Override
        public Set<Expression.Variable> variables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(condition.freeVariables());
            variables.addAll(ifStatement.variables());
            variables.addAll(elseStatement.variables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(ifStatement.writtenVariables());
            variables.addAll(elseStatement.writtenVariables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(condition.freeVariables());
            variables.addAll(ifStatement.readVariables());
            variables.addAll(elseStatement.readVariables());
            return variables;
        }

    }

    /**
     * A while statement is a statement of the form 
     * <code> while(b) { S } </code> where <code> b </code> is a condition and      
     * <code> S </code> is the statement that is meant to be executet as long as
     * the condition is fulfilled.
     */
    public static class While implements Statement {

        private final BooleanFormula condition;
        private final Statement statement;

        /**
         * Creates a while statement given the condition and the statement.
         *
         * @param condition a boolean formular
         * @param statement a statement to be executed as long as the condition
         * is met
         */
        public While(BooleanFormula condition, Statement statement) {
            this.condition = condition;
            this.statement = statement;
        }

        @Override
        public Configuration run(State state) {
            return new Configuration(new If(
                    condition,
                    new Composition(statement, this),
                    new Skip()
            ), state);
        }

        /**
         * @return the condition
         */
        public BooleanFormula getCondition() {
            return condition;
        }

        /**
         * @return the statement to be executed as long as the condition is met
         */
        public Statement getStatement() {
            return statement;
        }

        @Override
        public String toString() {
            return toString(false);
        }

        @Override
        public String toString(boolean latex) {
            return (latex ? "\\lst{while} ($" : "while (")
                    + condition.toString(latex) + (latex ? "$) \\{" : ") {")
                    + statement.toString(latex) + (latex ? "\\}" : "}");
        }

        @Override
        public Set<Expression.Variable> variables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(condition.freeVariables());
            variables.addAll(statement.variables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(statement.writtenVariables());
            return variables;
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            Set<Expression.Variable> variables = new HashSet<>();
            variables.addAll(condition.freeVariables());
            variables.addAll(statement.readVariables());
            return variables;
        }

    }

    /**
     * A black box is a statement of unknown execution behaviour. It can stand
     * for any statement. Most of the methods for statements cannot be called on
     * a black box.
     */
    public static class BlackBox implements Statement {

        private final String name;

        /**
         * Creates a new black box of the given name.
         * @param name 
         */
        public BlackBox(String name) {
            this.name = name;
        }

        @Override
        public Configuration run(State state) {
            throw new UnsupportedOperationException("You can't run a blackbox.");
        }

        @Override
        public String toString(boolean latex) {
            return (latex ? "$" : "") + name + (latex ? "$" : "");
        }

        @Override
        public Set<Expression.Variable> variables() {
            throw new UnsupportedOperationException("It is not known which variables a blackbox might contain.");
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            throw new UnsupportedOperationException("It is not known which variables a blackbox might write to.");
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            throw new UnsupportedOperationException("It is not known which variables a blackbox might reads from");
        }

    }

}
