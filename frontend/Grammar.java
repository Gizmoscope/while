package frontend;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author markus
 */
public class Grammar {

    public static Collection<Rule> toLL1(
            Collection<Rule> rules
    ) {
        final Map<NonTerminal, RuleTree> trees = new HashMap<>();
        rules.forEach(rule -> {
            if (!trees.containsKey(rule.getLhs())) {
                trees.put(rule.getLhs(), new RuleTree());
            }
            trees.get(rule.getLhs()).addRule(rule, rule.getRhs());
        });
        Collection<Rule> ll1Rules = new LinkedList<>();
        trees.keySet().forEach(n -> {
            ll1Rules.addAll(trees.get(n).generateLLRules(n));
        });
        return ll1Rules;
    }

    public static Collection<NonTerminal> extractNonTerminals(Collection<Rule> rules) {
        return rules.stream().map(r -> {
            Set<NonTerminal> nts = new HashSet<>();
            nts.add(r.getLhs());
            for(Variable var: r.getRhs()) {
                if(var instanceof NonTerminal) {
                    nts.add((NonTerminal) var);
                }
            }
            return nts;
        }).reduce((a, b) -> {
            Set<NonTerminal> union = new HashSet<>();
            union.addAll(a);
            union.addAll(b);
            return union;
        }).orElse(Collections.EMPTY_SET);
    }

    public static Collection<Token> extractTerminals(Collection<Rule> rules) {
        return rules.stream().map(r -> {
            Set<Token> ts = new HashSet<>();
            for(Variable var: r.getRhs()) {
                if(var instanceof Token) {
                    ts.add((Token) var);
                }
            }
            return ts;
        }).reduce((a, b) -> {
            Set<Token> union = new HashSet<>();
            union.addAll(a);
            union.addAll(b);
            return union;
        }).orElse(Collections.EMPTY_SET);
    }

    private static class RuleTree {

        private final Map<Variable, RuleTree> children;
        // The token can be null. It is only non-null if the path from the root
        // indeed forms the input string of the token
        private Rule rule;

        public RuleTree() {
            children = new HashMap<>();
        }

        void addRule(Rule rule, Variable[] vars) {
            if (vars.length == 0) {
                this.rule = rule;
            } else {
                Variable var = vars[0];
                if (!children.containsKey(var)) {
                    children.put(var, new RuleTree());
                }
                children.get(var).addRule(rule, Arrays.copyOfRange(vars, 1, vars.length));
            }
        }

        Collection<Rule> generateLLRules(NonTerminal lhs) {
            Collection<Rule> rules = new LinkedList<>();
            if (rule != null) {
                rules.add(new Rule(lhs));
            }
            children.keySet().forEach(var -> {
                NonTerminal newLhs = new NonTerminal(lhs.getName() + "[" + var + "]");
                rules.add(new Rule(lhs, var, newLhs));
                rules.addAll(children.get(var).generateLLRules(newLhs));
            });
            return rules;
        }

    }

}
