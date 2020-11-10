package vvhile.hoare;

import vvhile.intrep.Expression;
import vvhile.intrep.Statement;
import vvhile.intrep.Statement.*;
import static vvhile.hoare.BooleanFormula.*;

/**
 * This file contains the basic rules of the Hoare calculus
 *
 * @author markus
 */
public final class BasicHoare {

    private BasicHoare() {
    }

    /**
     * The skip axiom applies to statements of the form
     * <code> {P} skip {P} </code>.
     *
     * @return skip axiom
     */
    public static Axiom skipAxiom() {
        return new Axiom() {
            
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getProgram() instanceof Skip
                        && triple.getPreCondition().equals(triple.getPostCondition());
            }

            @Override
            public String toString() {
                return "skipAxiom";
            }

            @Override
            public String getShortName() {
                return "skip";
            }
        };
    }

    /**
     * The assignment axiom applies to statements of the form
     * <code> {P[t/x]} x := t {P} </code>.
     *
     * @return skip axiom
     */
    public static Axiom assignAxiom() {
        return new Axiom() {
            
            @Override
            public boolean applicable(HoareTriple triple) {
                if (triple.getProgram() instanceof Assignment) {
                    // Determine P[t/x]
                    Assignment assignment = (Assignment) triple.getProgram();
                    Expression.Variable x = assignment.getVariable();
                    Expression t = assignment.getExpression();
                    // check P[t/x] = P
                    return triple.getPreCondition().equals(triple.getPostCondition().subtitute(t, x));
                }
                return false;
            }

            @Override
            public String toString() {
                return "assignAxiom";
            }

            @Override
            public String getShortName() {
                return "ass";
            }
        };
    }

    /**
     * The composition rule applies to statements of the form
     * <code> {P} S;T {Q} </code>. It reduces the statement to
     * <code> {P} S {R} </code> and <code> {R} T {Q} </code>. The middle
     * condition <code> R </code> can be chosen as pleased. (Usually there are
     * canonical choices for <code> R </code>)
     *
     * @param middleCondition a boolean formular
     * @return composition rule with the given middle condition
     */
    public static Rule compRule(BooleanFormula middleCondition) {
        
        return new Rule() {

            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getProgram() instanceof Composition;
            }

            @Override
            public HoareTriple[] apply(HoareTriple triple) {
                if (triple.getProgram() instanceof Composition) {
                    // split the composition into its parts...
                    Composition composition = (Composition) triple.getProgram();
                    // ...and insert the middle condition resulting in two hoare triples
                    return new HoareTriple[]{
                        new HoareTriple(triple.getPreCondition(), composition.getFirstStatement(), middleCondition),
                        new HoareTriple(middleCondition, composition.getSecondStatement(), triple.getPostCondition())
                    };
                } else {
                    throw new IllegalArgumentException("This Rule is not applicable to the given Hoare triple.");
                }
            }

            @Override
            public String toString() {
                return "CompRule";
            }

            @Override
            public String getShortName() {
                return "comp";
            }
        };
    }


    /**
     * The if-then-else rule applies to statements of the form
     * <code> {P} if (b) {S} else {t} {Q} </code>. It reduces the statement to
     * <code> {P and b} S {Q} </code> and <code> {P and not b} T {Q} </code>.
     *
     * @return if-then-else rule with the given middle condition
     */
    public static Rule ifThenElseRule() {
        return new Rule() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getProgram() instanceof If;
            }

            @Override
            public HoareTriple[] apply(HoareTriple triple) {
                if (triple.getProgram() instanceof If) {
                    // retreive the substatements...
                    If ite = (If) triple.getProgram();
                    BooleanFormula b = ite.getCondition();
                    Statement s1 = ite.getIfStatement();
                    Statement s2 = ite.getElseStatement();
                    // ...and create the two according hoare triples
                    return new HoareTriple[]{
                        new HoareTriple(and(triple.getPreCondition(), b), s1, triple.getPostCondition()),
                        new HoareTriple(and(triple.getPreCondition(), not(b)), s2, triple.getPostCondition())
                    };
                } else {
                    throw new IllegalArgumentException("This Rule is not applicable to the given Hoare triple.");
                }
            }

            @Override
            public String toString() {
                return "ifThenElseRule";
            }

            @Override
            public String getShortName() {
                return "ite";
            }
        };
    }

    /**
     * The while rule applies to statements of the form 
     * <code> {I} while (b) {S} {I and not b} </code>.
     * It reduces the statement to
     * <code> {I and b} S {I} </code>
     * where <code>I</code> is a boolean formular, an invariant of the loop,
     * that has to be specified. Usually there is no canonical choice for 
     * <code>I</code>.
     * 
     * @param invariant a loop invariant
     * @return while rule
     */
    public static Rule whileRule(BooleanFormula invariant) {
        return new Rule() {
            
            @Override
            public boolean applicable(HoareTriple triple) {
                // Check the form: {I} while (b) {S} {I and not b}
                return triple.getProgram() instanceof While
                        && triple.getPreCondition().equals(invariant)
                        && triple.getPostCondition().equals(
                                and(invariant, not(((While) triple.getProgram()).getCondition())));
            }

            @Override
            public HoareTriple[] apply(HoareTriple triple) {
                if (applicable(triple)) {
                    // Retreive substatement and condition...
                    While whl = (While) triple.getProgram();
                    BooleanFormula b = whl.getCondition();
                    Statement s = whl.getStatement();
                    // ... and create the resulting hoare triple
                    return new HoareTriple[]{new HoareTriple(and(invariant, b), s, invariant)};
                } else {
                    throw new IllegalArgumentException("This Rule is not applicable to the given Hoare triple.");
                }
            }

            @Override
            public String toString() {
                return "whileRule";
            }

            @Override
            public String getShortName() {
                return "while";
            }
        };
    }

    /**
     * The consequence rule is applicable to any hoare triple and allows to
     * exchange the pre- and/or postcondition by other conditions. The new
     * precondition must imply the old one and the new postcondition must be
     * implied by the old one.
     * 
     * @param p1 new precondition
     * @param q1 new postcondition
     * @return consequence rule
     */
    public static Rule consRule(BooleanFormula p1, BooleanFormula q1) {
        return new Rule() {
            
            @Override
            public boolean applicable(HoareTriple triple) {
                return true;
            }

            @Override
            public HoareOrBoolean[] apply(HoareTriple triple) {
                // same pre- and post condition ==> no change
                if (triple.getPreCondition().equals(p1)
                        && triple.getPostCondition().equals(q1)) {
                    return new HoareOrBoolean[]{triple};
                } // same precondition ==> add one implications
                else if (triple.getPreCondition().equals(p1)
                        && !triple.getPostCondition().equals(q1)) {
                    return new HoareOrBoolean[]{
                        new HoareTriple(p1, triple.getProgram(), q1),
                        implies(q1, triple.getPostCondition())
                    };
                } // same postcondition ==> add one implications
                else if (!triple.getPreCondition().equals(p1)
                        && triple.getPostCondition().equals(q1)) {
                    return new HoareOrBoolean[]{
                        implies(triple.getPreCondition(), p1),
                        new HoareTriple(p1, triple.getProgram(), q1)
                    };
                } // new pre- and postcondition ==> add two implications
                else {
                    return new HoareOrBoolean[]{
                        implies(triple.getPreCondition(), p1),
                        new HoareTriple(p1, triple.getProgram(), q1),
                        implies(q1, triple.getPostCondition())
                    };
                }
            }

            @Override
            public String toString() {
                return "consRule";
            }

            @Override
            public String getShortName() {
                return "cons";
            }
        };
    }

    
    // TODO
    /**
     * 
     * 
     * @return black box
     */
    public static Rule blackBoxAxiom() {
        return new Axiom() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getProgram() instanceof Statement.BlackBox;
            }

            @Override
            public String toString() {
                return "blackBoxAxiom";
            }

            @Override
            public String getShortName() {
                return "blackBox";
            }
        };
    }

}
