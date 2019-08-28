package frontend;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import threading.BoundedBuffer;

/**
 *
 * @author markus
 */
public class ParseTree2 {

    private ProperNode root;
    private ProperNode active;
    private final BoundedBuffer<Parser2.ParseObject> parseStream;
    private final boolean topdown;
    private final Stack<Node> stack;
    private Thread thread;

    public ParseTree2(BoundedBuffer<Parser2.ParseObject> parseStream, boolean topdown) {
        this.parseStream = parseStream;
        this.topdown = topdown;
        this.stack = new Stack<>();
    }

    public void startRecompilation(BoundedBuffer<Parser2.ParseObject> buffer) {
        (thread = new Thread(() -> {
            boolean finished = false;
            Parser2.ParseObject next;
            do {
                next = parseStream.get();
                if (next instanceof Token) {
                    if (topdown) {
                        finished = addTokenTopDown((Token) next, buffer);
                    } else {
                        addTokenBottomUp((Token) next);
                        finished = Token.EOF.equals(next);
                    }
                } else if (next instanceof Rule) {
                    if (topdown) {
                        addRuleTopDown((Rule) next, buffer);
                    } else {
                        addRuleBottomUp((Rule) next);
                    }
                }
            } while (!finished);
        },
                (topdown ? "Top down " : "Bottom up ") + "parse tree"
        )).start();
        // TODO pass things to buffer.
    }

    private boolean addTokenTopDown(Token token, BoundedBuffer<Parser2.ParseObject> buffer) {
        if (active.fitsRhsTopDown(token)) {
            active.addChild(new Leaf(token, active));
            buffer.put(token);
        } else if (active.isFull() && active.getParent() != null) {
            active = active.getParent();
            return addTokenTopDown(token, buffer);
        } else if (active.parent == null && Token.EOF.equals(token)) {
            buffer.put(Token.EOF);
            return true;
        } else {
            throw new ParseException("Token doesn't fit into the active node: " + active.getRule() + " with " + token);
        }
        return false;
    }

    private void addTokenBottomUp(Token token) {
        stack.push(new Leaf(token, null));
    }

    private void addRuleTopDown(Rule rule, BoundedBuffer<Parser2.ParseObject> buffer) {
        if (root == null) {
            root = active = new ProperNode(rule, null);
        } else if (active.fitsRhsTopDown(rule.getLhs())) {
            ProperNode child = new ProperNode(rule, active);
            active.addChild(child);
            active = child;
            if (rule instanceof Grammar.RewriteRule) {
                Grammar.RewriteRule rewriteRule = (Grammar.RewriteRule) rule;
                buffer.put(rewriteRule.getOriginalRule());
            }
        } else if (active.isFull() && active.getParent() != null) {
            active = active.getParent();
            addRuleTopDown(rule, buffer);
        } else {
            throw new ParseException("Rule doesn't fit into the active node.");
        }
    }

    private void addRuleBottomUp(Rule rule) {
        root = new ProperNode(rule, null);
        while (!stack.isEmpty() && root.fitsRhsBottomUp(stack.peek())) {
            root.addChild(stack.pop());
        }
        if (root.children.size() == root.rule.getRhs().length) {
            stack.push(root);
        } else {
            throw new ParseException("Added Rules doesn't fit into new rule.");
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public ProperNode getRoot() {
        return root;
    }

    public void join() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ParseTree2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

        private boolean fitsRhsTopDown(Variable variable) {
            return children.size() < rule.getRhs().length
                    && rule.getRhs()[children.size()].equals(variable);
        }

        private boolean fitsRhsBottomUp(Node node) {
            if (children.size() >= rule.getRhs().length) {
                return false;
            } else if (node instanceof Leaf) {
                Leaf leaf = (Leaf) node;
                return rule.getRhs()[rule.getRhs().length - children.size() - 1].equals(leaf.token);
            } else if (node instanceof ProperNode) {
                ProperNode properNode = (ProperNode) node;
                return rule.getRhs()[rule.getRhs().length - children.size() - 1].equals(properNode.rule.getLhs());
            }
            return false;
        }

        private boolean isFull() {
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
            children.stream().forEach( child -> {
                builder.append(child.toString());
            }
            );
            return builder.toString();
//            builder.append("Node(").append(rule.getLhs().getName()).append(")[");
//            children.stream().forEach((child) -> {
//                builder.append(' ').append(child.toString());
//            });
//            return builder.append(" ]").toString();
        }

    }

}
