package vvhile.frontend;

import vvhile.intrep.ASTElementBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import vvhile.hoare.BooleanFormula;
import vvhile.intrep.Operator;
import vvhile.intrep.ASTElement;
import vvhile.intrep.Expression;

/**
 * A rule set (or set of rules) is essentially a grammar. However an instance of
 * this class also assigns an AST-builder to every rule.
 * 
 * @author markus
 */
public class RuleSet {

    /**
     * The unit builder is used by rules that don't have tokens in their right
     * hand side.
     */
    public static final ASTElementBuilder UNIT_BUILDER = new Unit();

    // All rules...
    private final List<Rule> rules;
    // ...and their AST-builders
    private final Map<Rule, ASTElementBuilder> builders;

    /**
     * Creates a new rule set.
     */
    public RuleSet() {
        rules = new LinkedList<>();
        builders = new HashMap<>();
    }

    /**
     * Adds a rule and the corresponding AST-builder to the rule set.
     * 
     * @param rule a rule
     * @param builder an AST-builder
     */
    public final void addRule(Rule rule, ASTElementBuilder builder) {
        rules.add(rule);
        builders.put(rule, builder);
    }

    /**
     * Adds a rule to the rule set that introduces a binary operator. The rule
     * and the corresponding AST-builder are calculated from the hirachical level
     * of the operator and the operator itself.
     * 
     * @param level hirachical level of the operator (= left hand side of the rule)
     * @param sublevel next hirachical level (are evaluated first)
     * @param token token representing the operator
     * @param operator an operator
     */
    public final void addOperatorRule(NonTerminal level, NonTerminal sublevel, Token token, Operator operator) {
        Rule rule = new Rule(level, sublevel, token, level);
        rules.add(rule);
        builders.put(rule, new FunctionBuilder(operator, token));
    }


    /**
     * Adds a rule to the rule set that introduces a unary operator. The rule
     * and the corresponding AST-builder are calculated from the hirachical level
     * of the operator and the operator itself.
     * 
     * @param level hirachical level of the operator (= left hand side of the rule)
     * @param token token representing the operator
     * @param operator an operator
     */
    public final void addUnaryOperatorRule(NonTerminal level, Token token, Operator operator) {
        Rule rule = new Rule(level, token, level);
        rules.add(rule);
        builders.put(rule, new FunctionBuilder(operator, token));
    }


    /**
     * Adds a rule to the rule set that introduces a constant. The rule
     * and the corresponding AST-builder are calculated from the hirachical level
     * of the constant and the constant itself.
     * 
     * @param level hirachical level of the constant (= left hand side of the rule)
     * @param token token representing the constant
     * @param constant a constant
     */
    public final void addConstantRule(NonTerminal level, Token token, Expression.Constant constant) {
        Rule rule = new Rule(level, token);
        rules.add(rule);
        builders.put(rule, new ConstantBuilder(constant, token));
    }

    /**
     * @param rule a rule
     * @return the AST-builder assigned to the rule
     */
    public ASTElementBuilder getBuilderFor(Rule rule) {
        return builders.get(rule);
    }

    /**
     * @return all rules of this rule set
     */
    public Collection<Rule> getRules() {
        return new LinkedList<>(rules);
    }

    /*
     * Unit builders are basically ignored by the parser as they reject all
     * tokens and produce null which is not added back to the parsing stack.
     */
    private static class Unit implements ASTElementBuilder {


        @Override
        public void put(ASTElement element) {
        }

        @Override
        public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
            return null;
        }

        @Override
        public boolean fits(ASTElement peek) {
            return false;
        }

    }

    /*
     * The constant builder transforms constant tokens into program constants.
     */
    private class ConstantBuilder implements ASTElementBuilder {

        private final Expression.Constant constant;
        private final Token token;
        private boolean constantRead;

        public ConstantBuilder(Expression.Constant constant, Token token) {
            this.constant = constant;
            this.token = token;
        }

        @Override
        public void put(ASTElement element) {
            // the builder remembers only if it has read a constant or not
            constantRead = true;
        }

        @Override
        public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
            // clean up and return constant
            constantRead = false;
            return constant;
        }

        @Override
        public boolean fits(ASTElement peek) {
            // the element can only fit if there wasn't already read one
            return !constantRead && token.equals(peek);
        }

    }


    /*
     * The function builder transforms operator tokens into program constants.
     */
    private class FunctionBuilder implements ASTElementBuilder {

        private final Operator op;
        private final Token token;
        // list of expressions read by this builder
        private final List<Expression> args;

        public FunctionBuilder(Operator op, Token token) {
            this.op = op;
            this.token = token;
            this.args = new ArrayList<>(op.getArgClasses().length);
        }

        @Override
        public void put(ASTElement element) {
            // The given element must be an expression, remember it
            if (element instanceof Expression) {
                args.add((Expression) element);
            }
        }

        @Override
        public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
            // reverse the read expressions as they are read in reversed order
            Collections.reverse(args);
            // Check if all arguments have been read
            if (args.size() != op.getArgClasses().length) {
                throw new IllegalArgumentException("Number of args and sorts don't match.");
            }
            // determine the sorts of the operator arguments...
            String[] sorts = new String[args.size()];
            for (int i = 0; i < args.size(); i++) {
                sorts[i] = op.getArgClasses()[i].getSimpleName();
                Expression expr = args.get(i);
                // ...and check if they agree with the sorts of the given expressions
                if (null != expr.getSort()) {
                    if (expr.getSort().equals(sorts[i]) || "Object".equals(sorts[i])) {
                        // everything is fine
                    } else if (expr.getSort().equals(Expression.SORT_UNKNOWN)) {
                        // this is the first occourance of this expression so
                        // assign the expected sort to it
                        args.set(i, expr.setSort(sorts[0]));
                    } else {
                        // the sorts conflict
                        throw new ParseException(
                                "Type conflict: Expression has to be " + sorts[i]
                                + " but was "
                                + expr.getSort() + ".");
                    }
                }
            }
            // Differentiate between boolean functions (returning booleans) and
            // all other functions. Then clean up and return the function.
            if (Expression.SORT_BOOLEAN.equals(op.getReturnClass().getSimpleName())) {
                BooleanFormula.BooleanFunction function = new BooleanFormula.BooleanFunction(
                        sorts, args.toArray(new Expression[args.size()]), op, true, parentheses
                );
                args.clear();
                return function;
            } else {
                Expression.Function function = new Expression.Function(
                        sorts, op.getReturnClass().getSimpleName(), args.toArray(new Expression[args.size()]), op, true, parentheses
                );
                args.clear();
                return function;
            }
        }

        @Override
        public boolean fits(ASTElement peek) {
            // The given element must be a token or an expression...
            if (peek instanceof Token.Symbol) {
                // ...meaning it is the operator symbol...
                // TODO the operator token should only be read once!
                return token.equals(peek);
            } else if (peek instanceof Expression) {
                // ...or an argument of the operator
                return args.size() < op.getArgClasses().length;
            }
            return false;
        }

    }
}
