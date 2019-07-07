package vvhile.basic.hoare;

import java.util.LinkedList;
import java.util.List;
import vvhile.intrep.Expression;
import vvhile.intrep.Statement;
import vvhile.intrep.Statement.*;
import static vvhile.basic.hoare.BasicHoare.*;
import vvhile.basic.operators.Operator;

/**
 *
 * @author markus
 */
public class HoareProver {

    private int blackboxes;

    public HoareProver() {
        this.blackboxes = 0;
    }

    public List<BooleanFormula> obligations(HoareTriple triple) {
        List<BooleanFormula> obligations = new LinkedList<>();
        obligations(triple, obligations);
        return obligations;
    }

    private void obligations(HoareTriple triple, List<BooleanFormula> obligations) {
        Rule rule = getRule(triple);
        HoareOrBoolean[] premises = rule.apply(triple);
        for (HoareOrBoolean premise : premises) {
            if (premise instanceof BooleanFormula) {
                obligations.add((BooleanFormula) premise);
            } else if (premise instanceof HoareTriple) {
                obligations((HoareTriple) premise, obligations);
            } else {
                throw new UnsupportedOperationException(
                        "Dont know what to do with instance of " + premise.getClass());
            }
        }
    }

    public HoareTree buildHoareTree(HoareTriple triple) {
        Rule rule = getRule(triple);
        if (rule == null) {
            return new HoareTree(triple, "...", new HoareTree(new BooleanFormula.BlackBox("...")));
        }
        HoareOrBoolean[] premises = rule.apply(triple);
        HoareTree[] children = new HoareTree[premises.length];
        for (int i = 0; i < premises.length; i++) {
            HoareOrBoolean premise = premises[i];
            if (premise instanceof BooleanFormula) {
                children[i] = new HoareTree(premise);
            } else if (premise instanceof HoareTriple) {
                children[i] = buildHoareTree((HoareTriple) premise);
            } else {
                throw new UnsupportedOperationException(
                        "Dont know what to do with instance of " + premise.getClass());
            }
        }
        return new HoareTree(triple, rule.getShortName(), children);
    }

    private BooleanFormula getWeakestLiberalPrecondition(Statement statement, BooleanFormula post) {
        if (statement instanceof Skip) {
            return post;
        } else if (statement instanceof Assignment) {
            Assignment assignment = (Assignment) statement;
            Expression.Variable x = assignment.getVariable();
            Expression t = assignment.getExpression();
            return (BooleanFormula) post.subtitute(t, x);
        } else if (statement instanceof Composition) {
            Composition composition = (Composition) statement;
            Statement s1 = composition.getFirstStatement();
            Statement s2 = composition.getSecondStatement();
            return getWeakestLiberalPrecondition(s1, getWeakestLiberalPrecondition(s2, post));
        } else if (statement instanceof If) {
            If ite = (If) statement;
            Expression b = ite.getCondition();
            Statement S1 = ite.getIfStatement();
            Statement S2 = ite.getElseStatement();
            return Operator.or(
                    Operator.and(b, getWeakestLiberalPrecondition(S1, post)),
                    Operator.and(Operator.not(b), getWeakestLiberalPrecondition(S2, post))
            );
        } else if (statement instanceof While) {
            return new BooleanFormula.BlackBox("I_{" + blackboxes++ + "}");
        } else if (statement instanceof BlackBox) {
            return new BooleanFormula.BlackBox("I_{" + blackboxes++ + "}");
        } else {
            return null;
        }
    }

    private Rule getRule(HoareTriple triple) {
        if (triple.getStatement() instanceof Skip) {
            if (skipAxiom().applicable(triple)) {
                return skipAxiom();
            } else {
                BooleanFormula p = getWeakestLiberalPrecondition(triple.getStatement(), triple.getPostCondition());
                BooleanFormula q = triple.getPostCondition();
                return consRule(p, q);
            }
        } else if (triple.getStatement() instanceof Assignment) {
            if (assignAxiom().applicable(triple)) {
                return assignAxiom();
            } else {
                BooleanFormula p = getWeakestLiberalPrecondition(triple.getStatement(), triple.getPostCondition());
                BooleanFormula q = triple.getPostCondition();
                return consRule(p, q);
            }
        } else if (triple.getStatement() instanceof Composition) {
            Composition composition = (Composition) triple.getStatement();
            Statement s1 = composition.getFirstStatement();
            Statement s2 = composition.getSecondStatement();
            return compRule(getWeakestLiberalPrecondition(s2, triple.getPostCondition()));
        } else if (triple.getStatement() instanceof If) {
            return ifThenElseRule();
        } else if (triple.getStatement() instanceof While) {
            While whl = (While) triple.getStatement();
            Expression b = whl.getCondition();
            if (whileRule(triple.getPreCondition()).applicable(triple)) {
                return whileRule(triple.getPreCondition());
            } else {
                BooleanFormula p = triple.getPreCondition();
                BooleanFormula q = Operator.and(triple.getPreCondition(), Operator.not(b));
                return consRule(p, q);
            }
        } else if (triple.getStatement() instanceof BlackBox) {
            return blackBoxAxiom();
        } else {
            return null;
//            throw new UnsupportedOperationException(
//                    "No Rule found for this Statement of type " + triple.getStatement().getClass());
        }
    }

}
