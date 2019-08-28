package frontend;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author markus
 */
public class Grammar {

    /**
     * Transforms a collection of rules into one that propably fulfills the
     * LL(1)-condition.
     *
     * @param rules set of rules
     * @return
     */
    public static Collection<Rule> toLL1(
            Collection<Rule> rules
    ) {
        // Create a rule-tree for each left hand side
        final Map<NonTerminal, RuleTree> trees = new HashMap<>();
        rules.forEach(rule -> {
            // If left hand side is not already captured, add it
            if (!trees.containsKey(rule.getLhs())) {
                trees.put(rule.getLhs(), new RuleTree());
            }
            // extend the tree by the rule
            trees.get(rule.getLhs()).addRule(rule, rule.getRhs());
        });
        // The new collection consists of all rules obtained by the rule trees 
        Collection<Rule> ll1Rules = new LinkedList<>();
        trees.keySet().forEach(n -> {
            ll1Rules.addAll(trees.get(n).generateLLRules(n));
        });
        // Remove empty rules
        return (ll1Rules);
    }

    private static Collection<Rule> removeEmptyRules(Collection<Rule> ll1Rules) {
        Set<RewriteRule> rewriteRules = ll1Rules.stream()
                .filter(rule -> rule instanceof RewriteRule)
                .map(rule -> (RewriteRule) rule)
                .collect(Collectors.toSet());

        Set<NonTerminal> nonterminals = rewriteRules.stream()
                .map(rule -> rule.getLhs())
                .collect(Collectors.toSet());
        ll1Rules.removeAll(rewriteRules);

        Set<Rule> wrongRules = ll1Rules.stream()
                .filter(rule -> containsNonterminalInRHS(rule, nonterminals))
                .collect(Collectors.toSet());
        ll1Rules.removeAll(wrongRules);

        wrongRules.forEach((rule) -> {
            ll1Rules.add(rewrite(rule, rewriteRules));
        });

        return ll1Rules;
    }

    private static boolean containsNonterminalInRHS(Rule rule, Collection<NonTerminal> nonterminals) {
        for (Variable v : rule.getRhs()) {
            if (v instanceof NonTerminal && nonterminals.contains((NonTerminal) v)) {
                return true;
            }
        }
        return false;
    }

    private static RewriteRule rewrite(Rule rule, Collection<RewriteRule> rewriteRules) {
        List<Variable> newRHS = new LinkedList<>();
        for (RewriteRule r : rewriteRules) {
            if (containsNonterminalInRHS(rule, Set.of(r.getLhs()))) {
                for (Variable v : rule.getRhs()) {
                    if (!v.equals(r.getLhs())) {
                        newRHS.add(v);
                    }
                }
                RewriteRule newRule = new RewriteRule(
                        r.originalRule,
                        rule.getLhs(),
                        newRHS.toArray(new Variable[newRHS.size()])
                );
                return newRule;
            }
        }
        return null;
    }

    public static Collection<NonTerminal> extractNonTerminals(Collection<Rule> rules) {
        return rules.stream().map(r -> {
            Set<NonTerminal> nts = new HashSet<>();
            nts.add(r.getLhs());
            for (Variable var : r.getRhs()) {
                if (var instanceof NonTerminal) {
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
            for (Variable var : r.getRhs()) {
                if (var instanceof Token) {
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
        // The rule can be null. It is only non-null if the path from the root
        // indeed forms the input string of the token
        private Rule rule;

        public RuleTree() {
            children = new HashMap<>();
        }

        void addRule(Rule rule, Variable[] vars) {
            if (vars.length == 0) {
                // reached the end of the right hand side of the rule
                this.rule = rule;
            } else {
                // add the first variable of the right hand side
                Variable var = vars[0];
                if (!children.containsKey(var)) {
                    children.put(var, new RuleTree());
                }
                // add a shortened version of the rule to the child accordingly
                children.get(var).addRule(rule, Arrays.copyOfRange(vars, 1, vars.length));
            }
        }

        /*
         * The generated rules are all of the form
         *    S -> V S'    or    S -> V
         * where V is a variable, i.e. the name of a branch, and S' is a 
         * recursively generated rule of the same form.
         */
        Collection<Rule> generateLLRules(NonTerminal lhs) {
            Collection<Rule> rules = new LinkedList<>();
            if (rule != null) {
                // The path within the branch traversed an actual rule added to the tree
                rules.add(new RewriteRule(rule, lhs));
            }
            children.keySet().forEach(var -> {
                // The new left hand side (S' in the description above)
                NonTerminal newLhs = new NonTerminal(lhs.getName() + "" + var + "");
                rules.add(new Rule(lhs, var, newLhs));
                // recursive call
                rules.addAll(children.get(var).generateLLRules(newLhs));
            });
            return rules;
        }

    }

    public static class RewriteRule extends Rule {

        private final Rule originalRule;

        public RewriteRule(Rule originalRule, NonTerminal lhs, Variable... rhs) {
            super(lhs, rhs);
            this.originalRule = originalRule;
        }

        public Rule getOriginalRule() {
            return originalRule;
        }

        @Override
        public String toString() {
            return super.toString() + "\t\t rewrites " + originalRule;
        }

    }

}
