package vvhile.frontend;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides all sort of utility to deal with formal grammars. The
 * main purpose is to transform a given grammar into one that fulfills the
 * LL(1)-property.
 *
 * @author markus
 */
public class Grammar {

    private Grammar() {
    }

    /**
     * Transforms a collection of rules into one that propably fulfills the
     * LL(1)-condition.
     *
     * @param rules set of rules
     * @return
     */
    public static Collection<Rule> toLL1(Collection<Rule> rules) {
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
        return ll1Rules;
    }

    /**
     * This function eliminates one level of left-recursion in a grammar. Left
     * recursive rules have the form
     * <pre>
     *     S -> S A_1 ... A_n
     * </pre> and are replaced by
     * <pre>
     *     S -> A_1 ... A_n
     *        | ε
     *     S' -> A_1 ... A_n
     * </pre> all other rules with S on their left hand side, e.g.
     * <pre>
     *     S -> B_1 ... B_m
     * </pre> are transformed to
     * <pre>
     *     S -> B_1 ... B_m S'
     * </pre> so that the number of rules increases by one.
     *
     * @param rules a set of rules
     * @return a (possibly) recursion-free set of rules generating the same
     * language
     */
    public static Collection<Rule> eliminateRecursion(Collection<Rule> rules) {
        final Set<Rule> result = new HashSet<>();
        // First sort all rules by their left hand side
        final Map<NonTerminal, Set<Rule>> rulesByLHS = new HashMap<>();
        rules.stream().forEach(rule -> {
            if (rulesByLHS.containsKey(rule.getLhs())) {
                rulesByLHS.get(rule.getLhs()).add(rule);
            } else {
                rulesByLHS.put(rule.getLhs(), new HashSet<>(Set.of(rule)));
            }
        });

        // Eliminate recursion for each left hand side separately
        rulesByLHS.keySet().stream().forEach(lhs -> {
            Set<Rule> rulesForLHS = rulesByLHS.get(lhs);
            // Find all rules with recursion, i.e. of the form S -> S A_1 ... A_n.
            Set<Rule> recursives = rulesForLHS.stream()
                    .filter(rule -> rule.getRhs().length > 0 && lhs.equals(rule.getRhs()[0]))
                    .collect(Collectors.toSet());
            // Apply the elemination process described above.
            recursives.stream().forEach(rule -> {
                // Remove the recursive rule
                rulesForLHS.remove(rule);
                Set<Rule> newRules = new HashSet<>();
                // and create two new rules (with a label indicating the recursion).
                NonTerminal newLhs = new NonTerminal(lhs.getName() + "_rec");
                // One rule is empty so that recursive calls can terminate. The 
                // second rule is the same as the recursive rule but misses the 
                // first variable on the right hand side (which caused the recursion).
                newRules.add(new RecursionRule(null, false, newLhs));
                newRules.add(new RecursionRule(rule, true, newLhs, Arrays.copyOfRange(rule.getRhs(), 1, rule.getRhs().length)));
                // All other rules are copied and the new left-hand-side-non-terminal
                // is added at the end of each of them.
                rulesForLHS.stream().forEach(r -> {
                    Variable[] newRhs = Arrays.copyOf(r.getRhs(), r.getRhs().length + 1);
                    newRhs[newRhs.length - 1] = newLhs;
                    newRules.add(new RecursionRule(r, false, r.getLhs(), newRhs));
                });
                // Remove all old rules and add the new ones instead
                rulesForLHS.clear();
                rulesForLHS.addAll(newRules);
            });
            result.addAll(rulesForLHS);
        });
        return result;
    }
    
    // TODO: Write comments from here on.

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

    private static Map<Variable, Collection<Token>> firsts(
            Collection<Rule> rules,
            Collection<NonTerminal> nonterminals,
            Collection<Token> terminals
    ) {
        Map<Variable, Collection<Token>> firsts = new HashMap<>();
        for (Token t : terminals) {
            Collection<Token> first = new AbstractCollection<Token>() {
                @Override
                public Iterator<Token> iterator() {
                    return new Iterator<Token>() {
                        boolean hasNext = true;

                        @Override
                        public boolean hasNext() {
                            if (hasNext) {
                                hasNext = false;
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public Token next() {
                            return t;
                        }
                    };
                }

                @Override
                public int size() {
                    return 1;
                }
            };
            firsts.put(t, first);
        }
        nonterminals.stream().forEach((n) -> {
            Collection<Token> first = new LinkedHashSet<>();
            firsts.put(n, first);
        });
        boolean change = true;
        while (change) {
            change = false;
            for (Rule rule : rules) {
                Collection<Token> rhs = new HashSet<>();
                Variable[] vs = rule.getRhs();
                if (vs.length == 0) {
                    rhs.add(EPSILON);
                } else {
                    rhs.addAll(firsts.get(vs[0]));
                    if (vs.length > 1) {
                        rhs.remove(EPSILON);
                    }
                }
                for (int i = 0; i < vs.length - 1; i++) {
                    if (firsts.get(vs[i]).contains(EPSILON)) {
                        rhs.addAll(firsts.get(vs[i + 1]));
                        rhs.remove(EPSILON);
                    } else {
                        break;
                    }
                    if (i == vs.length - 2 && firsts.get(vs[i + 1]).contains(EPSILON)) {
                        rhs.add(EPSILON);
                    }
                }
                if (!firsts.containsKey(rule.getLhs())) {
                    System.out.println(rule.getLhs());
                } else if (!firsts.get(rule.getLhs()).containsAll(rhs)) {
                    firsts.get(rule.getLhs()).addAll(rhs);
                    change = true;
                }
            }
        }
        return firsts;
    }

    private static Collection<Token> firstOfRule(Rule rule, Map<Variable, Collection<Token>> firsts) {
        Collection<Token> rhs = new LinkedHashSet<>();
        Variable[] vs = rule.getRhs();
        if (vs.length == 0) {
            rhs.add(EPSILON);
            return rhs;
        }
        rhs.addAll(firsts.get(vs[0]));
        if (vs.length > 1) {
            rhs.remove(EPSILON);
        }
        for (int i = 0; i < vs.length - 1; i++) {
            if (firsts.get(vs[i]).contains(EPSILON)) {
                rhs.addAll(firsts.get(vs[i + 1]));
                rhs.remove(EPSILON);
            } else {
                break;
            }
            if (i == vs.length - 2 && firsts.get(vs[i + 1]).contains(EPSILON)) {
                rhs.add(EPSILON);
            }
        }
        return rhs;
    }

    private static Map<NonTerminal, Collection<Token>> follows(
            Collection<Rule> rules,
            Map<Variable, Collection<Token>> firsts,
            Collection<NonTerminal> nonterminals,
            NonTerminal start
    ) {
        Map<NonTerminal, Collection<Token>> follows = new HashMap<>();
        nonterminals.stream().forEach((n) -> {
            Collection<Token> follow = new LinkedHashSet<>();
            if (start.equals(n)) {
                follow.add(Token.EOF);
            }
            follows.put(n, follow);
        });
        boolean change = true;
        while (change) {
            change = false;
            for (Rule rule : rules) {
                Variable[] vs = rule.getRhs();
                for (int i = 0; i < vs.length; i++) {
                    if (vs[i] instanceof NonTerminal) {
                        NonTerminal n = (NonTerminal) vs[i];
                        Variable[] c = new Variable[vs.length - i - 1];
                        Collection<Token> newfollow = new LinkedHashSet<>();
                        System.arraycopy(vs, i + 1, c, 0, c.length);
                        Collection<Token> firstOfC = firstOfRule(new Rule(n, c), firsts);
                        if (c.length > 0) {
                            newfollow.addAll(firstOfC);
                            newfollow.remove(EPSILON);
                        }
                        if (c.length == 0 || firstOfC.contains(EPSILON)) {
                            newfollow.addAll(follows.get(rule.getLhs()));
                        }
                        if (!follows.get(n).containsAll(newfollow)) {
                            follows.get(n).addAll(newfollow);
                            change = true;
                        }
                    }
                }
            }
        }
        return follows;
    }

    private static Collection<Token> firstplusOfRule(Rule rule,
            Map<Variable, Collection<Token>> firsts,
            Map<NonTerminal, Collection<Token>> follows) {
        Collection<Token> firstplus = new LinkedHashSet<>();
        firstplus.addAll(firstOfRule(rule, firsts));
        if (firstplus.contains(EPSILON)) {
            firstplus.addAll(follows.get(rule.getLhs()));
        }
        return firstplus;
    }

    public static ParsingTable generateParsingTable(RuleSet ruleSet, NonTerminal start) {
        Collection<Rule> rules = toLL1(eliminateRecursion(ruleSet.getRules()));
        return generateParsingTable(rules, extractNonTerminals(rules), extractTerminals(rules), start);
    }

    public static ParsingTable generateParsingTable(
            Collection<Rule> rules,
            Collection<NonTerminal> nonTerminals,
            Collection<Token> terminals,
            NonTerminal start
    ) {
        Map<Variable, Collection<Token>> firsts = firsts(rules, nonTerminals, terminals);
        Map<NonTerminal, Collection<Token>> follows = follows(rules, firsts, nonTerminals, start);
        ParsingTable table = new ParsingTable();
        rules.stream().forEach((rule) -> {
            Collection<Token> firstplus = firstplusOfRule(rule, firsts, follows);
            firstplus.stream().forEach((t) -> {
                table.addRule(rule.getLhs(), t, rule);
            });
        });
        return table;
    }

    /*
     * In a grammar there might be several places where the empty token is needed.
     * It has the name epsilon.
     */
    private static final Token EPSILON = new Token() {
        @Override
        public String toString() {
            return "ε";
        }
    };

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
            return super.toString() + " rewrites " + originalRule;
        }

    }

    public static class RecursionRule extends Rule {

        private final Rule originalRule;
        private final boolean head;

        public RecursionRule(Rule originalRule, boolean head, NonTerminal lhs, Variable... rhs) {
            super(lhs, rhs);
            this.originalRule = originalRule;
            this.head = head;
        }

        public Rule getOriginalRule() {
            return originalRule;
        }

        public boolean isHead() {
            return head;
        }

        @Override
        public String toString() {
            return super.toString() + (originalRule == null ? " recursion end" : (head ? " head for " : " rewrites ") + originalRule);
        }

    }

}
