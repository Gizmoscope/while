package vvhile.basic.language;

import java.util.Map;
import vvhile.hoare.BooleanFormula;
import static vvhile.basic.language.BasicTokens.*;
import vvhile.intrep.ASTElementBuilder;
import vvhile.frontend.NonTerminal;
import vvhile.frontend.Rule;
import vvhile.frontend.RuleSet;
import vvhile.frontend.Token;
import vvhile.frontend.Token.Identifier;
import vvhile.intrep.ASTElement;
import vvhile.intrep.Expression;
import vvhile.intrep.Statement;
import vvhile.intrep.StatementBuilder;
import vvhile.basic.language.IntegerScanner.Number;

/**
 * The Basic Rule Set contains all rules for the syntax of a basic programming
 * language, also known as WHILE. It contains the typical set of rules for
 * boolean and integral expressions with the usual operators. The language
 * itself consists of only five constructs: Assignments, Compositions,
 * Skip-Statments, If-Else- and While-Constructions.
 *
 * @author markus
 */
public class BasicRuleSet extends RuleSet {

    /**
     * Statement-Level: Language constructs
     */
    public static final NonTerminal STM = new NonTerminal("STM");
    /**
     * Expression-Level: May reduce to all sorts of comparison
     */
    public static final NonTerminal EXPR = new NonTerminal("EXPR");
    /**
     * Comparison-Level: May reduce to sums, differences or disjunctions
     */
    public static final NonTerminal COMP = new NonTerminal("COMP");
    /**
     * Term-Level: May reduce to products or conjunctions
     */
    public static final NonTerminal TERM = new NonTerminal("TERM");
    /**
     * Factor-Level: Handels bracketed/quantified expression, negations,
     * variables and constants
     */
    public static final NonTerminal FACT = new NonTerminal("FACT");

    // STM - The five language constructs
    private static final Rule STM_ASSIGN = new Rule(STM, /* -> */ ID, ASSIGN, EXPR);
    private static final Rule STM_SKIP = new Rule(STM, /* -> */ SKIP);
    private static final Rule STM_IF = new Rule(STM, /* -> */ IF, L_PAREN, EXPR, R_PAREN, L_CURLY, STM, R_CURLY, ELSE, L_CURLY, STM, R_CURLY);
    private static final Rule STM_WHILE = new Rule(STM, /* -> */ WHILE, L_PAREN, EXPR, R_PAREN, L_CURLY, STM, R_CURLY);
    private static final Rule STM_COMPOSITION = new Rule(STM, /* -> */ STM, SEMICOLON, STM);

    // FACT - The "more complicated" factor constructs
    private static final Rule FACT_PARENTHESES = new Rule(FACT, /* -> */ L_PAREN, EXPR, R_PAREN);
    private static final Rule FACT_FOR_ALL = new Rule(FACT, /* -> */ L_PAREN, FORALL, ID, DOT, EXPR, R_PAREN);
    private static final Rule FACT_EXISTS = new Rule(FACT, /* -> */ L_PAREN, EXISTS, ID, DOT, EXPR, R_PAREN);

    /**
     * Generate new rule set with the basic language- and expression-constructs
     * described above.
     */
    public BasicRuleSet() {
        super();

        // STM
        addRule(STM_ASSIGN, new StatementBuilder(
                args -> new Statement.Assignment((Expression.Variable) args[0], (Expression) args[2]),
                Token.Identifier.class, ASSIGN, Expression.class));
        addRule(STM_SKIP, new StatementBuilder(args -> new Statement.Skip(), SKIP));
        addRule(STM_IF, new StatementBuilder(
                args -> new Statement.If((BooleanFormula) args[2], (Statement) args[5], (Statement) args[9]),
                IF, L_PAREN, Expression.class, R_PAREN, L_CURLY, Statement.class, R_CURLY, ELSE, L_CURLY, Statement.class, R_CURLY));
        addRule(STM_WHILE, new StatementBuilder(
                args -> new Statement.While((BooleanFormula) args[2], (Statement) args[5]),
                WHILE, L_PAREN, Expression.class, R_PAREN, L_CURLY, Statement.class, R_CURLY));
        addRule(STM_COMPOSITION, new StatementBuilder(
                args -> new Statement.Composition((Statement) args[0], (Statement) args[2]),
                Statement.class, SEMICOLON, Statement.class));

        // FACT
        addRule(FACT_PARENTHESES, new ParenthesesBuilder());
        addRule(FACT_FOR_ALL, null);
        addRule(FACT_EXISTS, null);

        // EXPR
        addOperatorRule(EXPR, COMP, EQUALS, BasicOperators.EQUALS);
        addOperatorRule(EXPR, COMP, LESS_THAN, BasicOperators.LESS_THAN);
        addOperatorRule(EXPR, COMP, LESS_EQUAL, BasicOperators.LESS_EQUAL);
        addOperatorRule(EXPR, COMP, GREATER_EQUAL, BasicOperators.GREATER_EQUAL);
        addOperatorRule(EXPR, COMP, GREATER_THAN, BasicOperators.GREATER_THAN);
        addOperatorRule(EXPR, COMP, IMPLIES, BasicOperators.IMPLIES);
        addOperatorRule(EXPR, COMP, IMPLIED_BY, BasicOperators.IMPLIED_BY);
        addRule(new Rule(EXPR, COMP), RuleSet.UNIT_BUILDER);

        // COMP
        addOperatorRule(COMP, TERM, PLUS, BasicOperators.PLUS);
        addOperatorRule(COMP, TERM, MINUS, BasicOperators.MINUS);
        addOperatorRule(COMP, TERM, OR, BasicOperators.OR);
        addRule(new Rule(COMP, TERM), RuleSet.UNIT_BUILDER);

        // TERM
        addOperatorRule(TERM, FACT, TIMES, BasicOperators.TIMES);
        addOperatorRule(TERM, FACT, DIV, BasicOperators.DIV);
        addOperatorRule(TERM, FACT, AND, BasicOperators.AND);
        addRule(new Rule(TERM, FACT), RuleSet.UNIT_BUILDER);

        // FACT
        addRule(new Rule(FACT, NUM), new NumberBuilder());
        addRule(new Rule(FACT, ID), new IdentifierBuilder());
        addConstantRule(FACT, TRUE, new BooleanFormula.BooleanConstant(true));
        addConstantRule(FACT, FALSE, new BooleanFormula.BooleanConstant(false));
        addConstantRule(FACT, UNKNOWN, new BooleanFormula.BooleanConstant(null));
        addUnaryOperatorRule(FACT, NOT, BasicOperators.NOT);
        addUnaryOperatorRule(FACT, MINUS, BasicOperators.NEGATE);
    }

    /*
     * From this point on there are private implementions of the AST-element-builders
     * that turn the rules of this rule set into actual AST-elements.
     */

    /*
     * The identifier builder transforms indentifier tokens into program variables.
     */
    private class IdentifierBuilder implements ASTElementBuilder {

        private Token.Identifier identifier;

        @Override
        public void put(ASTElement element) {
            // The given element must be an identifier, remember it
            if (element instanceof Token.Identifier) {
                identifier = (Identifier) element;
            }
        }

        @Override
        public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
            Expression.Variable var;
            String name = identifier.getName();
            // look up the name of the variable and create a new one or retreive 
            // it from the global list of variables
            if (!vars.containsKey(name)) {
                vars.put(name, var = new Expression.Variable(Expression.SORT_UNKNOWN, name));
            } else {
                var = vars.get(name);
            }
            // clean up and return variable
            identifier = null;
            return var;
        }

        @Override
        public boolean fits(ASTElement peek) {
            // the element can only fit if there wasn't already read one
            return identifier == null && peek instanceof Token.Identifier;
        }

    }

    /*
     * The number builder transforms number tokens into program constants.
     */
    private class NumberBuilder implements ASTElementBuilder {

        private Number number;

        @Override
        public void put(ASTElement element) {
            // The given element must be a number token, remember it
            if (element instanceof Number) {
                number = (Number) element;
            }
        }

        @Override
        public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
            Expression.Constant constant = number.getConstant();
            // clean up and return constant
            number = null;
            return constant;
        }

        @Override
        public boolean fits(ASTElement peek) {
            // the element can only fit if there wasn't already read one
            return number == null && peek instanceof Number;
        }

    }

    /*
     * The parantheses builder transforms bracketed expressions into expressions.
     */
    private class ParenthesesBuilder implements ASTElementBuilder {

        private Expression expression;
        private int parenCount;

        @Override
        public void put(ASTElement element) {
            // The given element must be an expression or a parenthsis, remember
            // the expression
            if (element instanceof Expression) {
                expression = (Expression) element;
            } else if (L_PAREN.equals(element) || R_PAREN.equals(element)) {
                parenCount++;
            }
        }

        @Override
        public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
            Expression e = expression;
            // clean up and return expression
            parenCount = 0;
            expression = null;
            return e;
        }

        @Override
        public boolean fits(ASTElement peek) {
            // the element can only fit if there wasn't already an expression or
            // to many parentheses read
            if (peek instanceof Token.Symbol) {
                return parenCount < 2 && (L_PAREN.equals(peek) || R_PAREN.equals(peek));
            } else if (peek instanceof Expression) {
                return expression == null;
            }
            return false;
        }

    }
}
