package vvhile.hoare;

import java.util.LinkedList;
import java.util.List;
import vvhile.intrep.Expression;
import vvhile.intrep.Statement;
import vvhile.intrep.Statement.*;
import static vvhile.hoare.BasicHoare.*;
import static vvhile.hoare.BooleanFormula.*;

/**
 * The HoareProver class is the core of the semi-automated verification process.
 * Given a Hoare triple it builds a Hoare tree with a black box for every loop
 * invariant. Furthermore it calculates the weakest liberal precondition
 * everywhere else.
 *
 * @author markus
 */
public class HoareProver {

    // Loop invariants created along the construction are saved in this list
    private final List<BooleanFormula.BlackBox> blackBoxes;

    /**
     * Creates a new Hoare prover. To construct a Hoare tree a new instance of
     * this class should be created.
     */
    public HoareProver() {
        this.blackBoxes = new LinkedList<>();
    }

    /**
     * Calculates a list of obligation that have to be proven in order to verify
     * the given Hoare triple.
     *
     * @param triple a Hoare triple
     * @return a list of obligations
     */
    public List<BooleanFormula> obligations(HoareTriple triple) {
        List<BooleanFormula> obligations = new LinkedList<>();
        obligations(triple, obligations);
        return obligations;
    }

    /**
     * @return the list of all blackboxes that were introduced by the
     * buildHoareTree method.
     */
    public List<BooleanFormula.BlackBox> getBlackBoxes() {
        return blackBoxes;
    }

    /*
     * Recursively applies rules to the triple and adds all obligations to the
     * given list.
     */
    private void obligations(HoareTriple triple, List<BooleanFormula> obligations) {
        Rule rule = getRule(triple);
        HoareOrBoolean[] premises = rule.apply(triple);
        for (HoareOrBoolean premise : premises) {
            if (premise instanceof BooleanFormula) {
                obligations.add((BooleanFormula) premise);
            } else if (premise instanceof HoareTriple) {
                obligations((HoareTriple) premise, obligations);
            } else if (premise == null) {
                throw new NullPointerException("A rule has returned null as premise");
            } else {
                throw new UnsupportedOperationException(
                        "Dont know what to do with instance of " + premise.getClass());
            }
        }
    }

    /**
     * Given a Hoare triple this method creates the accoring Hoare tree. This
     * method works recursively. Black boxes are used for the loop invariants.
     * @param triple
     * @return 
     */
    public HoareTree buildHoareTree(HoareTriple triple) {
        // Determine an applicable Hoare rule for the Hoare triple
        Rule rule = getRule(triple);
        if (rule == null) {
            // No rule was found, use a black box
            return new HoareTree(triple, "...", new HoareTree(new BooleanFormula.BlackBox("...")));
        }
        // Apply the rule to determine the branches of the root
        HoareOrBoolean[] premises = rule.apply(triple);
        HoareTree[] children = new HoareTree[premises.length];
        for (int i = 0; i < premises.length; i++) {
            HoareOrBoolean premise = premises[i];
            if (premise instanceof BooleanFormula) {
                // The branch ends as the premise is a boolean formular, i.e. an obligation
                children[i] = new HoareTree(premise);
            } else if (premise instanceof HoareTriple) {
                // The branch continues as the premise is again a Hoare triple,
                // continue recursively
                children[i] = buildHoareTree((HoareTriple) premise);
            } else {
                throw new UnsupportedOperationException(
                        "Dont know what to do with instance of " + premise.getClass());
            }
        }
        return new HoareTree(triple, rule.getShortName(), children);
    }

    /*
     * Given a triple a statement and a post-condition, what is the weakest
     * pre-condition that makes the resulting Hoare triple valid? This is called
     * the weakest liberal pre-condition.
     * TODO: This should probably be part of the Statement class
     */
    private BooleanFormula getWeakestLiberalPrecondition(Statement statement, BooleanFormula post) {
        if (statement instanceof Skip) {
            // skip does not do anything, therefore pre = post
            return post;
        } else if (statement instanceof Assignment) {
            // An assignment changes a variable, "undo" this to get "the best" pre-condition
            Assignment assignment = (Assignment) statement;
            Expression.Variable x = assignment.getVariable();
            Expression t = assignment.getExpression();
            return (BooleanFormula) post.subtitute(t, x);
        } else if (statement instanceof Composition) {
            // For a composition "the best" pre-condition can be calulated one after the other
            Composition composition = (Composition) statement;
            Statement s1 = composition.getFirstStatement();
            Statement s2 = composition.getSecondStatement();
            return getWeakestLiberalPrecondition(s1, getWeakestLiberalPrecondition(s2, post));
        } else if (statement instanceof If) {
            // Reasonably combine "the best" pre-conditions of the if- and else-statement
            If ite = (If) statement;
            BooleanFormula b = ite.getCondition();
            Statement S1 = ite.getIfStatement();
            Statement S2 = ite.getElseStatement();
            return or(
                    and(b, getWeakestLiberalPrecondition(S1, post)),
                    and(not(b), getWeakestLiberalPrecondition(S2, post))
            );
        } else if (statement instanceof While) {
            // Sadly there is no good way to determine "the best" pre-conditions
            // for while-statements. The user has to find the loop-invariant himself.
            // Here a black box is created.
            return nextBlackBox();
        } else if (statement instanceof Statement.BlackBox) {
            return nextBlackBox();
        } else {
            return null;
        }
    }

    /*
     * Create a new black box with the index increased by one. Remember it.
     */
    private BooleanFormula.BlackBox nextBlackBox() {
        BooleanFormula.BlackBox blackBox = new BooleanFormula.BlackBox("I_{" + blackBoxes.size() + "}");
        blackBoxes.add(blackBox);
        return blackBox;
    }

    /*
     * What Hoare rule can be applied to the given triple?
     * TODO: This should probably be part of the Statement class
     */
    private Rule getRule(HoareTriple triple) {
        if (triple.getProgram() instanceof Skip) {
            // Try the skip axiom
            if (skipAxiom().applicable(triple)) {
                return skipAxiom();
            } else {
                // pre- and post-condition do not agree, return a cons-rule instead
                BooleanFormula p = getWeakestLiberalPrecondition(triple.getProgram(), triple.getPostCondition());
                BooleanFormula q = triple.getPostCondition();
                return consRule(p, q);
            }
        } else if (triple.getProgram() instanceof Assignment) {
            // Try the assign axiom
            if (assignAxiom().applicable(triple)) {
                return assignAxiom();
            } else {
                // pre-condition does not fit the post-condition, return a cons-rule instead
                BooleanFormula p = getWeakestLiberalPrecondition(triple.getProgram(), triple.getPostCondition());
                BooleanFormula q = triple.getPostCondition();
                return consRule(p, q);
            }
        } else if (triple.getProgram() instanceof Composition) {
            // Return the composition rule with "the best" middle condition
            Composition composition = (Composition) triple.getProgram();
            Statement s2 = composition.getSecondStatement();
            return compRule(getWeakestLiberalPrecondition(s2, triple.getPostCondition()));
        } else if (triple.getProgram() instanceof If) {
            // Return the if-then-else rule
            return ifThenElseRule();
        } else if (triple.getProgram() instanceof While) {
            While whl = (While) triple.getProgram();
            BooleanFormula b = whl.getCondition();
            // Check if a black box was created for the loop invariant
            if (triple.getPreCondition() instanceof BooleanFormula.BlackBox) {
                return whileRule(triple.getPreCondition());
            } else {
                // Introduce a black box and create a new cons-rule
                BooleanFormula p = getWeakestLiberalPrecondition(whl, triple.getPostCondition());
                BooleanFormula q = and(p, not(b));
                return consRule(p, q);
            }
        } else if (triple.getProgram() instanceof Statement.BlackBox) {
            return blackBoxAxiom();
        } else {
            return null;
//            throw new UnsupportedOperationException(
//                    "No Rule found for this Statement of type " + triple.getStatement().getClass());
        }
    }

}
