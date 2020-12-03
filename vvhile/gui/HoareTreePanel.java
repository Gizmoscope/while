package vvhile.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import vvhile.hoare.BooleanFormula;
import vvhile.hoare.HoareOrBoolean;
import vvhile.hoare.HoareTree;
import vvhile.hoare.HoareTriple;
import vvhile.util.Experimental;

/**
 * A Hoare tree panel is a graphical compontent visualizing a Hoare tree.
 *
 * @author markus
 */
@Experimental
public class HoareTreePanel extends JPanel {

    public static final Color BASIC_COLOR = Color.BLACK;
    public static final Color CONDITION_COLOR = Color.BLUE;
    public static final Color OBLIGATION_COLOR = Color.RED;
    public static final Color INVARIANT_COLOR = Color.GREEN.darker();

    private final Insets ruleInsets;
    private final HoareTree hoareTree;
    private final FontMetrics fontMetrics;
    private int fontSize = 24;

    public HoareTreePanel(Insets ruleInsets, HoareTree hoareTree) {
        this.ruleInsets = ruleInsets;
        this.hoareTree = hoareTree;
        setFont(getFont().deriveFont(1f * fontSize));
        this.fontMetrics = getFontMetrics(getFont());
    }

    private Dimension treeDimension(HoareTree tree) {
        HoareTree[] children = tree.getChildren();
        if (tree.getNode() instanceof BooleanFormula) {
            // no children => no horizontal line, no rule label
            return new Dimension(
                    fontMetrics.stringWidth(tree.getNode().toString()) + ruleInsets.left + ruleInsets.right,
                    fontSize + ruleInsets.top + ruleInsets.bottom
            );
        } else {
            // recursively calculate dimensions of children and add everything else
            int width = 0;
            int height = fontSize;
            for (HoareTree child : children) {
                Dimension d = treeDimension(child);
                // widths in trees add up
                width += d.width;
                // heights only max up
                height = Math.max(height, d.height);
            }
            return new Dimension(
                    Math.max(width, fontMetrics.stringWidth(tree.getNode().toString()))
                    //+ fontMetrics.stringWidth("[" + tree.getRule() + "]")
                    + ruleInsets.left + ruleInsets.right,
                    height + 2 + fontSize + ruleInsets.top + ruleInsets.bottom
            );
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return treeDimension(hoareTree);
    }

    private void drawHoareOrBoolean(Graphics g, HoareOrBoolean node, int width) {
        int offset = (width - fontMetrics.stringWidth(node.toString())) / 2;
        if (node instanceof BooleanFormula) {
            g.setColor(OBLIGATION_COLOR);
            g.drawString(node.toString(), offset, 0);
        } else if (node instanceof HoareTriple) {
            HoareTriple triple = (HoareTriple) node;
            String preCondition = "{" + triple.getPreCondition().toString() + "} ";
            String program = triple.getProgram().toString();
            String postCondition = " {" + triple.getPostCondition().toString() + "}";
            g.setColor(CONDITION_COLOR);
            g.drawString(preCondition, offset, 0);
            g.setColor(BASIC_COLOR);
            g.drawString(program, offset + fontMetrics.stringWidth(preCondition), 0);
            g.setColor(CONDITION_COLOR);
            g.drawString(postCondition, offset + fontMetrics.stringWidth(preCondition + program), 0);
        }
    }

    private void paintTree(Graphics g, HoareTree tree) {
        g.translate(ruleInsets.left, -ruleInsets.bottom);
        HoareTree[] children = tree.getChildren();
        String node = tree.getNode().toString();
        String rule = "[" + tree.getRule() + "]";
        int width = treeDimension(tree).width;// - fontMetrics.stringWidth(rule);
        if (tree.getNode() instanceof BooleanFormula) {
            drawHoareOrBoolean(g, tree.getNode(), width);
            g.translate(fontMetrics.stringWidth(node), 0);
        } else {
            int offset = width;
            for (HoareTree child : children) {
                offset -= treeDimension(child).width;
            }
            offset /= 2;
            g.setColor(BASIC_COLOR);
            g.drawLine(0, -fontSize - 1, width, -fontSize - 1);
            g.drawString(rule, width, -fontSize / 2 - 3);
            drawHoareOrBoolean(g, tree.getNode(), width);
            g.translate(offset, -2 - fontSize - ruleInsets.top);
            for (HoareTree child : children) {
                paintTree(g, child);
            }
            if (children.length == 0) {
                g.setColor(BASIC_COLOR);
                g.drawString("―", 0, 0);
            }
            g.translate(offset, 2 + fontSize + ruleInsets.top);
        }
        g.translate(ruleInsets.right, ruleInsets.bottom);
    }

    @Override
    public void paint(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        ((Graphics2D) g).setStroke(new BasicStroke(2f));
        Dimension d = treeDimension(hoareTree);
        g.translate(0, d.height);
        paintTree(g, hoareTree);
        g.translate(-d.width, 0);
    }
}
