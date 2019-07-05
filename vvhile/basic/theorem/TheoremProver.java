package vvhile.basic.theorem;

import java.util.HashSet;
import java.util.Set;
import vvhile.basic.Expression;
import vvhile.basic.Parser;
import vvhile.basic.hoare.BooleanFormula;
import vvhile.basic.operators.And;
import vvhile.basic.operators.ImpliedBy;
import vvhile.basic.operators.Implies;
import vvhile.basic.operators.Not;
import vvhile.basic.operators.Or;

/**
 *
 * @author markus
 */
public class TheoremProver {

    private final Set<BooleanFormula> truth;
    private final Set<ReplacementRule> rules;

    public TheoremProver() {
        this.truth = new HashSet<>();
        this.rules = new HashSet<>();
        addToTruth("true");

        addRule("!false", "true");
        addRule("false", "!true");

        addRule("false | false", "false");
        addRule("false | true", "true");
        addRule("true | false", "true");
        addRule("true | true", "true");

        addRule("false & false", "false");
        addRule("false & true", "false");
        addRule("true & false", "false");
        addRule("true & true", "true");

        addRule("a = a", "true", "a");

        addRule("a | !a", "true", "a");
        addRule("a & !a", "false", "a");
        addRule("false -> a", "true", "a");
        addRule("a -> true", "true", "a");
        addRule("a -> false", "!a", "a");
        addRule("true -> a", "a", "a");
        addRule("false -> a", "true", "a");
        addRule("a & b", "b & a", "a", "b");
        addRule("a | b", "b | a", "a", "b");
        
        addToTruth("(P(0) & (^n. P(n) -> P(n+1))) -> (^n. P(n))");
    }

    public void addToTruth(BooleanFormula formula) {
        truth.add(formula);
    }

    public void addRule(ReplacementRule rule) {
        rules.add(rule);
    }

    public final void addRule(String a, String b, String... variables) {
        Expression.Variable[] vars = new Expression.Variable[variables.length];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = new Expression.Variable(Expression.SORT_UNKNOWN, variables[i]);
        }
        rules.add(new ReplacementRule(
                Parser.compileExpression(a),
                Parser.compileExpression(b),
                vars));
    }

    public final void addToTruth(String formula) {
        truth.add((BooleanFormula) Parser.compileExpression(formula));
    }

    public boolean canProve(String formula, int depth) throws NoProofFoundException {
        return canProve((BooleanFormula) Parser.compileExpression(formula), depth);
    }

    public boolean prove(String formula, int depth) throws NoProofFoundException {
        return prove((BooleanFormula) Parser.compileExpression(formula), depth);
    }

    public boolean prove(BooleanFormula formula, int depth) throws NoProofFoundException {
        if (formula instanceof BooleanFormula.BooleanFunction) {
            BooleanFormula.BooleanFunction fun = (BooleanFormula.BooleanFunction) formula;
            Expression[] args = fun.getArgs();
            if (fun.getInterpretation() == And.AND) {
                return prove((BooleanFormula) args[0], depth) && prove((BooleanFormula) args[1], depth);
            } else if (fun.getInterpretation() == Or.OR) {
                return prove((BooleanFormula) args[0], depth) || prove((BooleanFormula) args[1], depth);
            } else if (fun.getInterpretation() == Not.NOT) {
                return !prove((BooleanFormula) args[0], depth);
            } else if (fun.getInterpretation() == Implies.IMPLIES) {
                return !prove((BooleanFormula) args[0], depth) || prove((BooleanFormula) args[1], depth);
            } else if (fun.getInterpretation() == ImpliedBy.IMPLIED_BY) {
                return prove((BooleanFormula) args[0], depth) || !prove((BooleanFormula) args[1], depth);
            } else {
                return canProve(formula, depth);
            }
        } else {
            return canProve(formula, depth);
        }
    }

    private boolean canProve(BooleanFormula formula, int depth) throws NoProofFoundException {
        if (depth == 0) {
            return truth.contains(formula);
        }
        Set<Expression> substitutes = new HashSet<>(formula.freeVariables());
        substitutes.addAll(formula.constants());
        for (ReplacementRule rule : rules) {
            Expression a = rule.a;
            Expression b = rule.b;
            Expression try1 = formula.trySubtitute(a, b);
            Expression try2 = formula.trySubtitute(b, a);
            if (try1 != null) {
                if (prove((BooleanFormula) try1, depth - 1)) {
                    return true;
                }
            }
            if (try2 != null) {
                if (prove((BooleanFormula) try2, depth - 1)) {
                    return true;
                }
            }
            for (Expression.Variable var : rule.variables) {
                for (Expression sub : substitutes) {
                    a = a.subtitute(sub, var);
                    b = b.subtitute(sub, var);
                    try1 = formula.trySubtitute(a, b);
                    try2 = formula.trySubtitute(b, a);
                    if (try1 != null) {
                        if (prove((BooleanFormula) try1, depth - 1)) {
                            return true;
                        }
                    }
                    if (try2 != null) {
                        if (prove((BooleanFormula) try2, depth - 1)) {
                            return true;
                        }
                    }
                }
            }
        }
        if(truth.contains(formula)) {
            return true;
        } else {
            throw new NoProofFoundException();
        }
    }

    @Override
    public String toString() {
        return "TheoremProver{" + "truth=" + truth + ", rules=" + rules + '}';
    }

    public static class ReplacementRule {

        private final Expression a;
        private final Expression b;
        private final Expression.Variable[] variables;

        public ReplacementRule(Expression a, Expression b, Expression.Variable... variables) {
            this.a = a;
            this.b = b;
            this.variables = variables;
        }

        @Override
        public String toString() {
            return "{" + a + "} ≡ {" + b + '}';
        }

    }

}
