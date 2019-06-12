package vvhile.basic;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author markus
 */
public class ParseTree {

    private ProperNode root;
    private ProperNode active;

    public ParseTree() {
    }

    public void addToken(Token token) {
        if (active.fitsRhs(token)) {
            active.addChild(new Leaf(token, active));
        } else if (active.isFull() && active.getParent() != null) {
            active = active.getParent();
            addToken(token);
        } else {
            throw new ParseException("Token doesn't fit into the active node");
        }
    }

    public void addRule(Rule rule) {
        if (root == null) {
            root = active = new ProperNode(rule, null);
        } else if (active.fitsRhs(rule.getLhs())) {
            ProperNode child = new ProperNode(rule, active);
            active.addChild(child);
            active = child;
        } else if (active.isFull() && active.getParent() != null) {
            active = active.getParent();
            addRule(rule);
        } else {
            throw new ParseException("Rule doesn't fit into the active node");
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public ProperNode getRoot() {
        return root;
    }

    public static interface Node {

        ProperNode getParent();

    }

    public static class Leaf implements Node {

        private final Token token;
        private final ProperNode parent;

        public Leaf(Token token, ProperNode parent) {
            this.token = token;
            this.parent = parent;
        }

        @Override
        public ProperNode getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return token.toString();
        }

        public Token getToken() {
            return token;
        }

    }

    public static class ProperNode implements Node {

        private final Rule rule;
        private final ProperNode parent;

        private final List<Node> children;

        public ProperNode(Rule rule, ProperNode parent) {
            this.rule = rule;
            this.parent = parent;
            this.children = new LinkedList<>();
        }

        public boolean fitsRhs(Variable variable) {
            return children.size() < rule.getRhs().length
                    && rule.getRhs()[children.size()].equals(variable);
        }

        public boolean isFull() {
            return children.size() == rule.getRhs().length;
        }

        private void addChild(Node node) {
            children.add(node);
        }

        @Override
        public ProperNode getParent() {
            return parent;
        }

        public Rule getRule() {
            return rule;
        }

        public List<Node> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Node(").append(rule.getLhs().getName()).append(")[");
            children.stream().forEach((child) -> {
                builder.append(' ').append(child.toString());
            });
            return builder.append(" ]").toString();
        }

    }

}
