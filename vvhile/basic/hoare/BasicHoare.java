package vvhile.basic.hoare;

import vvhile.basic.Expression;
import vvhile.basic.Statement;
import vvhile.basic.Statement.*;
import static vvhile.basic.operators.Operator.*;

/**
 *
 * @author markus
 */
public final class BasicHoare {

    private BasicHoare() {
    }

    public static Axiom skipAxiom() {
        return new Axiom() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getStatement() instanceof Skip
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

    public static Axiom assignAxiom() {
        return new Axiom() {
            @Override
            public boolean applicable(HoareTriple triple) {
                if (triple.getStatement() instanceof Assignment) {
                    Assignment assignment = (Assignment) triple.getStatement();
                    Expression.Variable x = assignment.getVariable();
                    Expression t = assignment.getExpression();
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

    public static Rule compRule(BooleanFormula middleCondition) {
        return new Rule() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getStatement() instanceof Composition;
            }

            @Override
            public HoareTriple[] apply(HoareTriple triple) {
                if (triple.getStatement() instanceof Composition) {
                    Composition composition = (Composition) triple.getStatement();
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

    public static Rule ifThenElseRule() {
        return new Rule() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getStatement() instanceof If;
            }

            @Override
            public HoareTriple[] apply(HoareTriple triple) {
                if (triple.getStatement() instanceof If) {
                    If ite = (If) triple.getStatement();
                    Expression b = ite.getCondition();
                    Statement s1 = ite.getIfStatement();
                    Statement s2 = ite.getElseStatement();
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

    public static Rule whileRule(BooleanFormula invariant) {
        return new Rule() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getStatement() instanceof While
                        && triple.getPreCondition().equals(invariant)
                        && triple.getPostCondition().equals(
                                and(invariant, not(((While) triple.getStatement()).getCondition())));
            }

            @Override
            public HoareTriple[] apply(HoareTriple triple) {
                if (applicable(triple)) {
                    While whl = (While) triple.getStatement();
                    Expression b = whl.getCondition();
                    Statement s = whl.getStatement();
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

    public static Rule consRule(BooleanFormula p1, BooleanFormula q1) {
        return new Rule() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return true;
            }

            @Override
            public HoareOrBoolean[] apply(HoareTriple triple) {
                if (triple.getPreCondition().equals(p1)
                        && triple.getPostCondition().equals(q1)) {
                    return new HoareOrBoolean[]{triple};
                } else if (triple.getPreCondition().equals(p1)
                        && !triple.getPostCondition().equals(q1)) {
                    return new HoareOrBoolean[]{
                        new HoareTriple(p1, triple.getStatement(), q1),
                        implies(q1, triple.getPostCondition())
                    };
                }else if (!triple.getPreCondition().equals(p1)
                        && triple.getPostCondition().equals(q1)) {
                    return new HoareOrBoolean[]{
                        implies(triple.getPreCondition(), p1),
                        new HoareTriple(p1, triple.getStatement(), q1)
                    };
                }else {
                    return new HoareOrBoolean[]{
                        implies(triple.getPreCondition(), p1),
                        new HoareTriple(p1, triple.getStatement(), q1),
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

    public static Rule blackBoxAxiom() {
        return new Axiom() {
            @Override
            public boolean applicable(HoareTriple triple) {
                return triple.getStatement() instanceof BlackBox;
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
