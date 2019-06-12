package vvhile.basic.hoare;

import vvhile.basic.Statement;

public class HoareTriple implements HoareOrBoolean {

    private final BooleanFormula preCondition;
    private final Statement statement;
    private final BooleanFormula postCondition;
    private final BooleanFormula excPostCondition;

    public HoareTriple(BooleanFormula preCondition, Statement statement, BooleanFormula postCondition) {
        this.preCondition = preCondition;
        this.statement = statement;
        this.postCondition = postCondition;
        this.excPostCondition = new BooleanFormula.BooleanConstant(true);
    }

    public BooleanFormula getPreCondition() {
        return preCondition;
    }

    public Statement getStatement() {
        return statement;
    }

    public BooleanFormula getPostCondition() {
        return postCondition;
    }

    public BooleanFormula getExcPostCondition() {
        return excPostCondition;
    }

    @Override
    public String toString() {
        return "{" + preCondition + "} " + statement + " {" + postCondition + "} ▷ " + excPostCondition;
    }

    @Override
    public String toString(boolean latex) {
        return (latex ? "\\textcolor{blue}{$\\{" : "{")
                + preCondition.toString(latex)+ (latex ?"\\}$} " : "} ")
                + statement.toString(latex)+ (latex ? " \\textcolor{blue}{$\\{" : " {")
                + postCondition.toString(latex)+ (latex ? "$\\}}" : "}"); // \\vartriangleright " 
        //+ excPostCondition.toLatexCode() + "$";
    }

}
