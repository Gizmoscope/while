package vvhile.intrep;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author markus
 */
public interface Statement {

    public Configuration run(State state);

    public String toString(boolean latex);

    public Set<Expression.Variable> variables();

    public Set<Expression.Variable> writtenVariables();

    public Set<Expression.Variable> readVariables();

    public Set<Statement> subStatements();

    public static class Assignment implements Statement {

        private final Expression.Variable variable;
        private final Expression expression;

        public Assignment(Expression.Variable variable, Expression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        @Override
        public Configuration run(State state) {
            return new Configuration(null,
                    state.substitute(expression.getValue(state), variable));
        }

        @Override
        public String toString() {
            return toString(false);
        }

        public Expression.Variable getVariable() {
            return variable;
        }

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

        @Override
        public Set<Statement> subStatements() {
            Set<Statement> statements = new HashSet<>();
            statements.add(this);
            return statements;
        }

    }

    public static class Skip implements Statement {

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

        @Override
        public Set<Statement> subStatements() {
            Set<Statement> statements = new HashSet<>();
            statements.add(this);
            return statements;
        }

    }

    public static class Composition implements Statement {

        private final Statement firstStatement;
        private final Statement secondStatement;

        public Composition(Statement firstStatement, Statement secondStatement) {
            this.firstStatement = firstStatement;
            this.secondStatement = secondStatement;
        }

        @Override
        public Configuration run(State state) {
            Configuration newConf = firstStatement.run(state);
            if (newConf.getProgram() == null) {
                return new Configuration(secondStatement, newConf.getState());
            } else {
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

        public Statement getFirstStatement() {
            return firstStatement;
        }

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

        @Override
        public Set<Statement> subStatements() {
            Set<Statement> statements = new HashSet<>();
            statements.add(this);
            statements.addAll(firstStatement.subStatements());
            statements.addAll(secondStatement.subStatements());
            return statements;
        }

    }

    public static class If implements Statement {

        private final Expression condition;
        private final Statement ifStatement;
        private final Statement elseStatement;

        public If(Expression condition, Statement ifStatement, Statement elseStatement) {
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

        public Expression getCondition() {
            return condition;
        }

        public Statement getIfStatement() {
            return ifStatement;
        }

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

        @Override
        public Set<Statement> subStatements() {
            Set<Statement> statements = new HashSet<>();
            statements.add(this);
            statements.addAll(ifStatement.subStatements());
            statements.addAll(elseStatement.subStatements());
            return statements;
        }

    }

    public static class While implements Statement {

        private final Expression condition;
        private final Statement statement;

        public While(Expression condition, Statement statement) {
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

        public Expression getCondition() {
            return condition;
        }

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
                    + condition.toString(latex) + (latex ? "$) \\{" : "= {")
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

        @Override
        public Set<Statement> subStatements() {
            Set<Statement> statements = new HashSet<>();
            statements.add(this);
            statements.addAll(statement.subStatements());
            return statements;
        }

    }

    public static class BlackBox implements Statement {

        private final String name;

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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set<Expression.Variable> writtenVariables() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set<Expression.Variable> readVariables() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set<Statement> subStatements() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
