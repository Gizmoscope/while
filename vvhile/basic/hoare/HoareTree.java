package vvhile.basic.hoare;

import java.util.LinkedList;
import java.util.List;
import vvhile.basic.Statement;
import vvhile.basic.hoare.BooleanFormula.BooleanFunction;

/**
 *
 * @author markus
 */
public class HoareTree {

    private final HoareOrBoolean node;
    private final HoareTree[] children;
    private final String rule;

    public HoareTree(HoareOrBoolean node) {
        this.node = node;
        this.children = new HoareTree[0];
        this.rule = "";
    }

    public HoareTree(HoareTriple node, HoareTree... children) {
        this.node = node;
        this.children = children;
        this.rule = "";
    }

    public HoareTree(HoareOrBoolean node, String rule, HoareTree... children) {
        this.node = node;
        this.children = children;
        this.rule = rule;
    }

    public List<BooleanFormula> getObligations() {
        List<BooleanFormula> obligations = new LinkedList();
        if (node instanceof BooleanFormula) {
            obligations.add((BooleanFormula) node);
        } else {
            for (HoareTree child : children) {
                obligations.addAll(child.getObligations());
            }
        }
        return obligations;
    }

    public AnnotatedStatement toProofOutline() {
        AnnotatedStatement[] subOutlines = new AnnotatedStatement[children.length];
        for (int i = 0; i < children.length; i++) {
            subOutlines[i] = children[i].toProofOutline();
        }
        if (node instanceof BooleanFormula) {
//            BooleanFormula booleanFormula = (BooleanFormula) node;
//            return new AnnotatedStatement(null, new Annotation(false, booleanFormula));
            return null;
        } else if (node instanceof HoareTriple) {
            HoareTriple hoareTriple = (HoareTriple) node;
            Statement statement = hoareTriple.getStatement();
            if (children.length == 3 && children[0].node instanceof BooleanFunction) {
                return new AnnotatedStatement(subOutlines[1],
                        new Annotation(false, (BooleanFormula) ((BooleanFunction) children[0].node).getArgs()[0]),
                        null,
                        new Annotation(false, (BooleanFormula) ((BooleanFunction) children[2].node).getArgs()[1]));
            } else if (children.length == 2 && children[0].node instanceof BooleanFunction) {
                return new AnnotatedStatement(subOutlines[1],
                        new Annotation(false, (BooleanFormula) ((BooleanFunction) children[0].node).getArgs()[0]),
                        null,
                        null);
            } else if (children.length == 2 && children[1].node instanceof BooleanFunction) {
                return new AnnotatedStatement(subOutlines[0],
                        null,
                        null,
                        new Annotation(false, (BooleanFormula) ((BooleanFunction) children[1].node).getArgs()[1]));
            }
            if (statement instanceof Statement.Skip
                    || statement instanceof Statement.Assignment
                    || statement instanceof Statement.BlackBox) {
                return new AnnotatedStatement(
                        statement,
                        new Annotation(false, hoareTriple.getPreCondition()),
                        null,
                        new Annotation(false, hoareTriple.getPostCondition())
                );
            } else if (statement instanceof Statement.Composition) {
                return new AnnotatedStatement(
                        new Statement.Composition(subOutlines[0], subOutlines[1]),
                        new Annotation(false, hoareTriple.getPreCondition()),
                        null,
                        new Annotation(false, hoareTriple.getPostCondition())
                );
            } else if (statement instanceof Statement.If) {
                Statement.If ite = (Statement.If) statement;
                return new AnnotatedStatement(
                        new Statement.If(ite.getCondition(), subOutlines[0], subOutlines[1]),
                        new Annotation(false, hoareTriple.getPreCondition()),
                        null,
                        new Annotation(false, hoareTriple.getPostCondition())
                );
            } else if (statement instanceof Statement.While) {
                Statement.While vvhile = (Statement.While) statement;
                return new AnnotatedStatement(
                        new Statement.While(vvhile.getCondition(), subOutlines[0]),
                        new Annotation(true, hoareTriple.getPreCondition()),
                        null,
                        new Annotation(false, hoareTriple.getPostCondition())
                );
            } else {
                throw new UnsupportedOperationException(
                        "Dont know what to do with instance of " + statement.getClass());
            }
        } else {
            throw new UnsupportedOperationException(
                    "Dont know what to do with instance of " + node.getClass());
        }
    }

    private int stringWidth() {
        if (node instanceof BooleanFormula) {
            return node.toString().replace("⟹", "->").length();
        } else {
            int nodeWidth = node.toString().length();
            int childWidth = -3;
            for (HoareTree child : children) {
                childWidth += child.stringWidth() + 3;
            }
            return Math.max(nodeWidth, childWidth);
        }
    }

    private int height() {
        if (node instanceof BooleanFormula) {
            return 1;
        } else {
            int height = 0;
            for (HoareTree child : children) {
                height = Math.max(height, child.height());
            }
            return height;
        }
    }

    private String repeat(int length, char c) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    private String concatenate(String a, String b) {
        String[] aLines = a.split("\n");
        String[] bLines = b.split("\n");
        String[] newLines = new String[Math.max(aLines.length, bLines.length)];
        for (int i = 0; i < Math.min(aLines.length, bLines.length); i++) {
            newLines[i] = aLines[i] + "   " + bLines[i];
        }
        if (aLines.length > bLines.length) {
            for (int i = bLines.length; i < aLines.length; i++) {
                newLines[i] = aLines[i] + "   " + repeat(bLines[0].replace("⟹", "->").length(), ' ');
            }
        }
        if (bLines.length > aLines.length) {
            for (int i = aLines.length; i < bLines.length; i++) {
                newLines[i] = repeat(aLines[0].replace("⟹", "->").length(), ' ') + "   " + bLines[i];
            }
        }
        StringBuilder builder = new StringBuilder(newLines[0]);
        for (int i = 1; i < newLines.length; i++) {
            builder.append('\n').append(newLines[i]);
        }
        return builder.toString();
    }

    public String toString(boolean latex) {
        if (latex) {
            if (children.length == 0) {
                if (node instanceof BooleanFormula) {
                    return "\\AxiomC{\\textcolor{red}{$" + node.toString(true) + "$}}";
                } else {
                    return "\\AxiomC{--}\n\\LeftLabel{[" + rule + "]}\\UnaryInfC{" + node.toString(true) + "}";
                }
            } else {
                String subtrees = children[0].toString(true) + "\n";
                for (int i = 1; i < children.length; i++) {
                    subtrees = subtrees + children[i].toString(true) + "\n";
                }
                String inf = "\\LeftLabel{[" + rule + "]}";
                inf += children.length == 1 ? "\\UnaryInfC{"
                        : children.length == 2 ? "\\BinaryInfC{" : "\\TernaryInfC{";
                return subtrees + inf + node.toString(true) + "}";
            }
        } else {
            return toString();
        }
    }

    @Override
    public String toString() {
        if (children.length == 0) {
            if (node instanceof BooleanFormula) {
                return node.toString();
            } else {
                return node.toString() + '\n' + repeat(stringWidth(), '―') + "\n  ―――" + repeat(node.toString().length() - 5, ' ');
            }
        } else {
            String subtrees = children[0].toString();
            for (int i = 1; i < children.length; i++) {
                subtrees = concatenate(subtrees, children[i].toString());
            }
            int diff = stringWidth() - node.toString().replace("⟹", "->").length();
            if (diff > 0) {
                return node.toString() + repeat(diff, ' ') + '\n' + repeat(stringWidth(), '―') + '\n' + subtrees;
            } else {
                return node.toString() + '\n' + repeat(stringWidth(), '―') + '\n' + concatenate(subtrees, repeat(-diff, ' '));
            }
        }
    }

}
