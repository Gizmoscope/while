package vvhile.hoare;

import vvhile.intrep.Statement;

/**
 * A Hoare triple consists of a program and two conditions. Essentially a Hoare
 * triple is a boolean statement which is to be interpreted as follows. if the
 * precondition is valid then, after executing the program, the postcondition is
 * valid. If a Hoare triple itself is valid has to be proved (using the Hoare
 * calculus).
 *
 * @author markus
 */
public class HoareTriple implements HoareOrBoolean {

    private final BooleanFormula preCondition;
    private final Statement program;
    private final BooleanFormula postCondition;

    /**
     * @param preCondition a boolean formular
     * @param program a program
     * @param postCondition a boolean formular
     */
    public HoareTriple(BooleanFormula preCondition, Statement program, BooleanFormula postCondition) {
        this.preCondition = preCondition;
        this.program = program;
        this.postCondition = postCondition;
    }

    /**
     * @return the precondition
     */
    public BooleanFormula getPreCondition() {
        return preCondition;
    }

    /**
     * @return the program
     */
    public Statement getProgram() {
        return program;
    }

    /**
     * @return the postcondition
     */
    public BooleanFormula getPostCondition() {
        return postCondition;
    }

    @Override
    public String toString() {
        return "{" + preCondition + "} " + program + " {" + postCondition + "}";
    }

    /**
     * Returns a string representation of the Hoare triple. In case it is formated
     * as LaTeX code the pre- and postconditions have a blue text color.
     * 
     * @param latex true if the string should represent LaTeX code
     * @return a string representation of the Hoare triple
     */
    @Override
    public String toString(boolean latex) {
        return (latex ? "\\textcolor{blue}{$\\{" : "{")
                + preCondition.toString(latex) + (latex ? "\\}$} " : "} ")
                + program.toString(latex) + (latex ? " \\textcolor{blue}{$\\{" : " {")
                + postCondition.toString(latex) + (latex ? "$\\}}" : "}");
    }

}
