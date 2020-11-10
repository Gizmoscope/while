package vvhile.intrep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.event.ChangeListener;
import static vvhile.basic.language.BasicTokens.L_PAREN;
import vvhile.frontend.Grammar;
import vvhile.frontend.Parser;
import vvhile.frontend.Rule;
import vvhile.frontend.RuleSet;
import vvhile.frontend.Token;
import vvhile.frontend.Variable;
import vvhile.util.BoundedBuffer;

/**
 * An AST-compiler compiles data from a parse tree to an abstract syntax tree.
 *
 * @author markus
 */
public class ASTCompiler {

    private final RuleSet ruleSet;
    private final BoundedBuffer<Parser.ParseObject> parseStream;
    private final Stack<ASTElement> stack;
    private final Stack<Object> save;
    private Thread thread;
    private final Map<String, Expression.Variable> vars;

    /**
     * Creates a new AST-compiler. It reads the parse objects from the given
     * buffer and either puts them on an internal stack or applies AST-builder
     * to the data from the stack to build up the abstract syntax tree.
     *
     * @param parseStream
     * @param ruleSet
     */
    public ASTCompiler(BoundedBuffer<Parser.ParseObject> parseStream, RuleSet ruleSet) {
        this.parseStream = parseStream;
        this.stack = new Stack<>();
        this.save = new Stack<>();
        this.ruleSet = ruleSet;
        this.vars = new HashMap<>();
    }

    public void startCompilation(BoundedBuffer<Parser.ParseObject> buffer) {
        Runnable r = () -> {
            boolean finished = false;
            Parser.ParseObject next;
            do {
                next = parseStream.get();
                if (next instanceof Token) {
                    addTokenBottomUp((Token) next);
                    finished = Token.EOF.equals(next);
                } else if (next instanceof Rule) {
                    addRuleBottomUp((Rule) next);
                }
            } while (!finished);
            // Remove the eof-token
            stack.pop();
            thread = null;
        };
        // Start the thread.
        (thread = new Thread(r, "AST-Compiler")).start();
        // TODO pass things to buffer.
    }

    private void addTokenBottomUp(Token token) {
        stack.push(token);
    }

    /*
     * Apply a rule to the stack.
     */
    private void addRuleBottomUp(Rule rule) {
        // In contrast to rewrite rules, recursion rules are treated here
        if (rule instanceof Grammar.RecursionRule) {
            Grammar.RecursionRule recursion = (Grammar.RecursionRule) rule;
            // The recursion rule is the head if it comes from the original rule
            // causing the recursion (and is not the recursion end).
            if (recursion.isHead()) {
                // The original rule will be applied later since the first part,
                // i.e. the recursion part, of the rule hasn't been translated yet.
                // Push the original rule and its tail onto the save-stack.
                save.push(recursion.getOriginalRule());
                for (Variable v : recursion.getRhs()) {
                    save.push(stack.pop());
                }
            } else if (recursion.getOriginalRule() != null) {
                // If the original rule is null, then that was the recursion end
                // which can be ignored. All other rules can be treated as normal.
                addRuleBottomUp(recursion.getOriginalRule());
            }
        } else {
            // Find the builder for the rule
            ASTElementBuilder builder = ruleSet.getBuilderFor(rule);
            // Take elements from the stack as long as they fit the rule
            while (!stack.isEmpty() && builder.fits(stack.peek())) {
                // Feed the builder with the information from the stack
                builder.put(stack.pop());
                listeners.forEach(l -> l.stateChanged(null));
                listeners.forEach(l -> l.stateChanged(null));
            }
            // With all information the builder can produce an object of the
            // intermediate representation.
            ASTElement element = builder.build(vars, useParantheses());
            // Unit builder don't produce objects. In all other cases the result
            // is added to the stack again.
            if (element != null) {
                stack.push(element);
                listeners.forEach(l -> l.stateChanged(null));
            } else {
            }
            // If there was recursion this is treated now.
            while (!save.isEmpty()) {
                Object top = save.pop();
                if (top instanceof ASTElement) {
                    // Restore saved elements.
                    stack.push((ASTElement) top);
                    listeners.forEach(l -> l.stateChanged(null));
                } else if (top instanceof Rule) {
                    // Apply the defered (recursion) rule.
                    addRuleBottomUp((Rule) top);
                }
            }
        }
    }

    private boolean useParantheses() {
        if (stack.isEmpty()) {
            return false;
        } else {
            // TODO: This is not exactly the condition I want. I can create 
            // parantheses where there weren't any before.
            return L_PAREN.equals(stack.peek());
        }
    }

    @Override
    public String toString() {
        return "" + getRoot();
    }

    public ASTElement getRoot() {
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * Wait for the termination of the compilation.
     */
    public void join() {
        while (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }
    }

    public String[] getStack() {
        return stack.stream().map(elem -> elem.toString()).toArray(n -> new String[n]);
    }

    // Only for visualisation purposes
    private final Set<ChangeListener> listeners = new HashSet<>();

    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

}
