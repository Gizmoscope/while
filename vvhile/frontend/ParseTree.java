package vvhile.frontend;

import java.util.LinkedList;
import java.util.List;
import vvhile.util.BoundedBuffer;

/**
 * <p>
 * A parse tree is build from rules and variable replacements. The root of the
 * tree is given by a rule. For each variable on the right hand side of the rule
 * there is a child. Leafs in the tree are given by tokens. However, during the
 * building process the children of a node can be null. A node given by a rule,
 * i.e. a node that will have children at some point, is called proper node.
 * </p>
 *
 * <p>
 * Usually the parse tree is used to retranslate rules, converted to
 * LL(1)-rules, to their original form.
 * </p>
 *
 * <p>
 * A parse tree is build top-down.
 * </p>
 *
 * @author markus
 */
public class ParseTree {

    // root of the tree
    private ProperNode root;
    // marker for the building process (where to insert children)
    private ProperNode active;

    // source of data for building up the tree
    private final BoundedBuffer<Parser.ParseObject> input;
    // thread object
    private Thread thread;

    /**
     * Creates a new parse tree. It retreives its data from the given input
     * buffer.
     *
     * @param input source of data for building up the tree
     */
    public ParseTree(BoundedBuffer<Parser.ParseObject> input) {
        this.input = input;
    }

    /**
     * Start retranslating the data given by the input buffer. The data is used
     * to build up the tree. Token are directly passed to the given output
     * buffer. As soon as a rule can be retranslated, the original rule is
     * passed to the output buffer. This has the effect that output is generated
     * bottom-up.
     *
     * @param buffer output buffer
     */
    public void startRetranslation(BoundedBuffer<Parser.ParseObject> buffer) {
        Runnable r = () -> {
            boolean finished = false;
            Parser.ParseObject next;
            do {
                // Retreive data.
                next = input.get();
                // It is either a token or a rule
                if (next instanceof Token) {
                    // if the data was the eof-token or a message that stops the
                    // translation, finished is set to true
                    finished = addToken((Token) next, buffer);
                } else if (next instanceof Rule) {
                    addRule((Rule) next, buffer);
                }
            } while (!finished);
            thread = null;
        };
        // Start the thread.
        (thread = new Thread(r, "Parse tree")).start();
    }

    /*
     * Add a token. Return true if the retranslation should be stopped.
     */
    private boolean addToken(Token token, BoundedBuffer<Parser.ParseObject> buffer) {
        if (active.fits(token)) {
            // The token fits into the active rule. Add it and pass it to the output.
            active.addChild(new Leaf(token, active));
            buffer.put(token);
        } else if (active.isFull() && active.getParent() != null) {
            // The active rule is already filled with children. Go up one level and repeat.
            active = active.getParent();
            return addToken(token, buffer);
        } else if (active.parent == null) {
            if (Token.EOF.equals(token)) {
                // The parse tree is build up completely and the eof-token was read.
                buffer.put(Token.EOF);
                return true;
            } else {
                throw new ParseException("EOF was read but parse tree is not complete.");
            }
        } else {
            throw new ParseException("Token doesn't fit into the active node: " + active.getRule() + " with " + token);
        }
        return false;
    }

    private void addRule(Rule rule, BoundedBuffer<Parser.ParseObject> buffer) {
        if (root == null) {
            // This is the first rule, so it becomes the root.
            root = active = new ProperNode(rule, null);
        } else if (active.fits(rule.getLhs())) {
            // The left hand side of the rule fits the active rule. Add it. Make it the new active.
            ProperNode child = new ProperNode(rule, active);
            active.addChild(child);
            active = child;
            // If the rule is a rewrite rule, then this means that a token that
            // were already put to this tree actually belong to the original rule
            // wrapped by the rewrite rule. Pass the original to the output.
            if (rule instanceof Grammar.RewriteRule) {
                Grammar.RewriteRule rewriteRule = (Grammar.RewriteRule) rule;
                buffer.put(rewriteRule.getOriginalRule());
            }
        } else if (active.isFull() && active.getParent() != null) {
            // active rule is already filled with children. Go up one level and repeat.
            active = active.getParent();
            addRule(rule, buffer);
        } else {
            throw new ParseException("Rule doesn't fit into the active node.");
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }

    /**
     * @return the root of this tree.
     */
    public ProperNode getRoot() {
        return root;
    }

    /**
     * Wait for the termination of the retranslation.
     */
    public void join() {
        while (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * The parse tree consists of nodes given by this interface. There are leafs
     * and proper nodes.
     */
    public static interface Node {

        /**
         * @return the parent of this node. Might be null if this node is the 
         * root of the tree.
         */
        ProperNode getParent();

    }
    
    /**
     * A leaf is a note that cannot have children. In a parse tree the leaves
     * represent a token.
     */
    public static class Leaf implements Node {

        private final Token token;
        private final ProperNode parent;

        /**
         * Creates a new leaf given the token and the parent node.
         * 
         * @param token the token
         * @param parent the parent
         */
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

        /**
         * @return the token
         */
        public Token getToken() {
            return token;
        }

    }

    /**
     * A proper node is a node that is supposed to have children. In a parse tree
     * proper nodes prepresent rules.
     */
    public static class ProperNode implements Node {

        private final Rule rule;
        private final ProperNode parent;
        private final List<Node> children;

        /**
         * Creates a new proper node with given the rule and the parent. Initially
         * this node has no children. They must be added afterwards.
         * 
         * @param rule the rule
         * @param parent the parent
         */
        public ProperNode(Rule rule, ProperNode parent) {
            this.rule = rule;
            this.parent = parent;
            this.children = new LinkedList<>();
        }

        /*
         * Checks if the given variable fits into the rule. It must agree with
         * the next variable of the RHS of the rule. If a child is added this
         * methods looks at the following variable in the RHS of the rule and so
         * on.
         */
        private boolean fits(Variable variable) {
            return children.size() < rule.getRhs().length
                    && rule.getRhs()[children.size()].equals(variable);
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

        /**
         * @return the rule
         */
        public Rule getRule() {
            return rule;
        }

        /**
         * @return the list of all added children
         */
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            children.stream().forEach(child -> {
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
