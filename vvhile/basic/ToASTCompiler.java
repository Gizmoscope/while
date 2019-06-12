package vvhile.basic;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static vvhile.basic.BasicRuleSet.*;
import vvhile.basic.ParseTree.Leaf;
import vvhile.basic.ParseTree.Node;
import vvhile.basic.ParseTree.ProperNode;
import static vvhile.basic.Token.*;
import vvhile.basic.hoare.BooleanFormula;
import vvhile.basic.operators.And;
import vvhile.basic.operators.Equals;
import vvhile.basic.operators.GreaterEqual;
import vvhile.basic.operators.GreaterThan;
import vvhile.basic.operators.ImpliedBy;
import vvhile.basic.operators.Implies;
import vvhile.basic.operators.LessEqual;
import vvhile.basic.operators.LessThan;
import vvhile.basic.operators.Minus;
import vvhile.basic.operators.Negate;
import vvhile.basic.operators.Not;
import vvhile.basic.operators.Operator;
import vvhile.basic.operators.Or;
import vvhile.basic.operators.Plus;
import vvhile.basic.operators.Times;

/**
 *
 * @author markus
 */
public class ToASTCompiler {

    public static Statement compileStatement(ParseTree parseTree) {
        ParseTree.ProperNode root = parseTree.getRoot();
        return ToASTCompiler.compileStatement(root, new HashMap<>());
    }

    public static Expression compileExpression(ParseTree parseTree) {
        ParseTree.ProperNode root = parseTree.getRoot();
        return compileExpression(root, false, new HashMap<>());
    }

    private static Statement compileStatement(ParseTree.Node node, Map<String, Expression.Variable> vars) {
        if (node instanceof ParseTree.ProperNode) {
            ParseTree.ProperNode pNode = (ParseTree.ProperNode) node;
            Rule rule = pNode.getRule();
            Variable[] vs = rule.getRhs();
            if (vs.length == 0) {
                return null;
            }
            List<Node> children = pNode.getChildren();
            Statement first = null;
            Statement next = null;

            // Assign
            if (vs[0] instanceof Token.Identifier) {
                Identifier id = (Identifier) ((Leaf) children.get(0)).getToken();
                String name = id.getName();
                Expression.Variable variable;
                if (vars.containsKey(name)) {
                    variable = vars.get(name);
                } else {
                    variable = new Expression.Variable(Expression.SORT_UNKNOWN, id.getName());
                    vars.put(name, variable);
                }
                pNode = (ParseTree.ProperNode) children.get(1);
                rule = pNode.getRule();
                vs = rule.getRhs();
                if (vs.length == 0) {
                    return null;
                }
                children = pNode.getChildren();
                if (children.size() == 3) {
                    Expression value = compileExpression(children.get(1), false, vars);
                    if (Expression.SORT_UNKNOWN.equals(variable.getSort())) {
                        variable = (Expression.Variable) variable.setSort(value.getSort());
                        vars.put(name, variable);
                    } else if (!variable.getSort().equals(value.getSort())) {
                        throw new ParseException("Type conflict: Expression has to be "
                                + variable.getSort()
                                + " but was " + value.getSort() + ".");
                    }
                    first = new Statement.Assignment(variable, value);
                    next = ToASTCompiler.compileStatement(children.get(2), vars);
                } else {
                    first = new Statement.BlackBox(name);
                    next = ToASTCompiler.compileStatement(children.get(0), vars);
                }
            } // Skip
            if (SKIP.equals(vs[0])) {
                first = new Statement.Skip();
                next = ToASTCompiler.compileStatement(children.get(1), vars);
            } // If
            else if (IF.equals(vs[0])) {
                first = new Statement.If(
                        compileExpression(children.get(2), false, vars),
                        ToASTCompiler.compileStatement(children.get(5), vars),
                        ToASTCompiler.compileStatement(children.get(9), vars)
                );
                next = ToASTCompiler.compileStatement(children.get(11), vars);
            } // Try
            else if (WHILE.equals(vs[0])) {
                first = new Statement.While(
                        compileExpression(children.get(2), false, vars),
                        ToASTCompiler.compileStatement(children.get(5), vars)
                );
                next = ToASTCompiler.compileStatement(children.get(7), vars);
            } // Composition
            else if (SEMICOLON.equals(vs[0])) {
                return ToASTCompiler.compileStatement(children.get(1), vars);
            } // Variables
            else if (vs[0] instanceof Identifier) {
                Identifier id = (Identifier) ((Leaf) children.get(0)).getToken();
                next = ToASTCompiler.compileStatement(children.get(1), vars);
            }
            return next == null ? first
                    : new Statement.Composition(first, next);
        }
        return null;
    }

    private static Expression.Variable getVariable(Identifier id, Map<String, Expression.Variable> vars) {
        String name = id.getName();
        Expression.Variable variable;
        if (vars.containsKey(name)) {
            variable = vars.get(name);
        } else {
            variable = new Expression.Variable(Expression.SORT_UNKNOWN, name);
            vars.put(name, variable);
        }
        return variable;
    }

    private static Expression compileExpression(ParseTree.Node node, boolean parentheses, Map<String, Expression.Variable> vars) {
        if (node instanceof ParseTree.ProperNode) {
            ParseTree.ProperNode pNode = (ParseTree.ProperNode) node;
            Rule rule = pNode.getRule();
            Variable[] vs = rule.getRhs();
            if (vs.length == 0) {
                return null;
            }
            List<Node> children = pNode.getChildren();
            // Facts
            if (vs[0] instanceof Token.Identifier) {
                Token.Identifier id = (Token.Identifier) ((Leaf) children.get(0)).getToken();
                return new Expression.Variable(Expression.SORT_UNKNOWN, id.getName());
            } else if (vs[0] instanceof Token.Number) {
                Token.Number num = (Token.Number) ((Leaf) children.get(0)).getToken();
                return num.getConstant();
            } else if (TRUE.equals(vs[0])) {
                return new BooleanFormula.BooleanConstant(true);
            } else if (FALSE.equals(vs[0])) {
                return new BooleanFormula.BooleanConstant(false);
            } else if (UNKNOWN.equals(vs[0])) {
                return new BooleanFormula.BooleanConstant(null);
            } else if (NOT.equals(vs[0])) {
                Expression[] args = {compileExpression(children.get(1), false, vars)};
                return compileFunction(Not.NOT, args, parentheses, vars);
            } else if (MINUS.equals(vs[0])) {
                Expression[] args = {compileExpression(children.get(1), false, vars)};
                return compileFunction(Negate.NEGATE, args, parentheses, vars);
            } else if (L_PAREN.equals(vs[0])) {
                return compileExpression(children.get(1), true, vars);
            } else if (EXPR.equals(vs[0])) {
                return compileExpression(children.get(0), parentheses, vars);
            } else if (FORALL.equals(vs[0])) {
                Identifier id = (Identifier) ((Leaf) children.get(1)).getToken();
                return new Expression.Quantifier(true,
                        getVariable(id, vars),
                        compileExpression(children.get(3), false, vars));
            } else if (EXISTS.equals(vs[0])) {
                Identifier id = (Identifier) ((Leaf) children.get(1)).getToken();
                return new Expression.Quantifier(false,
                        getVariable(id, vars),
                        compileExpression(children.get(3), false, vars));
            } // The rest
            else if (COMP.equals(vs[0]) || TERM.equals(vs[0]) || FACT.equals(vs[0])) {
                Expression first = compileExpression(children.get(0), parentheses, vars);
                if (children.get(1) instanceof ProperNode) {
                    ProperNode prime = (ProperNode) children.get(1);
                    if (prime.getChildren().isEmpty()) {
                        return first;
                    } else {
                        Expression second = compileExpression(prime.getChildren().get(1), false, vars);
                        Expression[] args = {first, second};
                        Node opNode = prime.getChildren().get(0);
                        if (!(opNode instanceof Leaf)) {
                            // TODO this must not happen
                            return null;
                        }
                        Token t = ((Leaf) opNode).getToken();
                        Operator op = null;
                        if (LESS_THAN.equals(t)) {
                            op = LessThan.LESS_THAN;
                        } else if (LESS_EQUAL.equals(t)) {
                            op = LessEqual.LESS_EQUAL;
                        } else if (EQUALS.equals(t)) {
                            if (Expression.SORT_BOOLEAN.equals(args[0].getSort())) {
                                op = new Equals(Boolean.class);
                            } else if (Expression.SORT_INTEGER.equals(args[0].getSort())) {
                                op = new Equals(BigInteger.class);
                            } else if (Expression.SORT_SET.equals(args[0].getSort())) {
                                op = new Equals(Set.class);
                            } else if (Expression.SORT_LIST.equals(args[0].getSort())) {
                                op = new Equals(List.class);
                            } else if (Expression.SORT_BOOLEAN.equals(args[1].getSort())) {
                                op = new Equals(Boolean.class);
                            } else if (Expression.SORT_INTEGER.equals(args[1].getSort())) {
                                op = new Equals(BigInteger.class);
                            } else if (Expression.SORT_SET.equals(args[1].getSort())) {
                                op = new Equals(Set.class);
                            } else if (Expression.SORT_LIST.equals(args[1].getSort())) {
                                op = new Equals(List.class);
                            } else {
                                op = new Equals(Object.class);
                            }
                        } else if (GREATER_EQUAL.equals(t)) {
                            op = GreaterEqual.GREATER_EQUAL;
                        } else if (GREATER_THAN.equals(t)) {
                            op = GreaterThan.GREATER_THAN;
                        } else if (IMPLIES.equals(t)) {
                            op = Implies.IMPLIES;
                        } else if (IMPLIED_BY.equals(t)) {
                            op = ImpliedBy.IMPLIED_BY;
                        } else if (PLUS.equals(t)) {
                            op = Plus.PLUS;
                        } else if (MINUS.equals(t)) {
                            op = Minus.MINUS;
                        } else if (OR.equals(t)) {
                            op = Or.OR;
                        } else if (TIMES.equals(t)) {
                            op = Times.TIMES;
                        } else if (AND.equals(t)) {
                            op = And.AND;
                        }
                        return compileFunction(op, args, parentheses, vars);
                    }
                }
            }
        }

        return null;
    }

    private static Expression.Function compileFunction(Operator op, Expression[] args, boolean parentheses, Map<String, Expression.Variable> vars) {
        if (args.length != op.getArgClasses().length) {
            throw new IllegalArgumentException("Number of args and sorts don't match.");
        }
        String[] sorts = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            sorts[i] = op.getArgClasses()[i].getSimpleName();
            Expression expr = args[i];
            if (null != expr.getSort()) {
                if (expr.getSort().equals(sorts[i]) || "Object".equals(sorts[i])) {
                } else if (expr.getSort().equals(Expression.SORT_UNKNOWN)) {
                    args[i] = expr.setSort(sorts[0]);
                } else {
                    throw new ParseException(
                            "Type conflict: Expression has to be " + sorts[i]
                            + " but was "
                            + expr.getSort() + ".");
                }
            }
        }
        if (Expression.SORT_BOOLEAN.equals(op.getReturnClass().getSimpleName())) {
            return new BooleanFormula.BooleanFunction(sorts, args, op, true, parentheses);
        } else {
            return new Expression.Function(sorts, op.getReturnClass().getSimpleName(), args, op, true, parentheses);
        }
    }

}
