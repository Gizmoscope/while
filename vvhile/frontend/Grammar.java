package vvhile.frontend;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import vvhile.util.SingletonCollection;

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

    /**
     * Given a collection of rules this method extracts all non-terminals
     * appearing as a left hand side or somewhere in a right hand side.
     *
     * @param rules a collection of rules
     * @return all non-terminals appearing in the rules
     */
    public static Collection<NonTerminal> extractNonTerminals(Collection<Rule> rules) {
        return rules.stream().map(r -> {
            // iterate over all rules...
            Set<NonTerminal> nts = new HashSet<>();
            // ...add the left hand side...
            nts.add(r.getLhs());
            // ...and all non-terminals of the right hand side
            for (Variable var : r.getRhs()) {
                if (var instanceof NonTerminal) {
                    nts.add((NonTerminal) var);
                }
            }
            return nts;
        }).reduce((a, b) -> {
            // unite all non-terminals
            Set<NonTerminal> union = new HashSet<>();
            union.addAll(a);
            union.addAll(b);
            return union;
        }).orElse(Collections.EMPTY_SET);
    }

    /**
     * Given a collection of rules this method extracts all terminals appearing
     * somewhere in a right hand side.
     *
     * @param rules a collection of rules
     * @return all terminals appearing in the rules
     */
    public static Collection<Token> extractTerminals(Collection<Rule> rules) {
        return rules.stream().map(r -> {
            // iterate over all rules...
            Set<Token> ts = new HashSet<>();
            // ...add all terminals of the right hand side
            for (Variable var : r.getRhs()) {
                if (var instanceof Token) {
                    ts.add((Token) var);
                }
            }
            return ts;
        }).reduce((a, b) -> {
            // unite all non-terminals
            Set<Token> union = new HashSet<>();
            union.addAll(a);
            union.addAll(b);
            return union;
        }).orElse(Collections.EMPTY_SET);
    }

    /*
     * The firsts map assigns to every variable a list of tokens. It is the list
     * of those tokens that might appear first when deriving the variable acording
     * to the set of rules. The empty word token is possible as well.
     */
    private static Map<Variable, Collection<Token>> firsts(
            Collection<Rule> rules,
            Collection<NonTerminal> nonterminals,
            Collection<Token> terminals
    ) {
        // Initally, the list of first is empty
        Map<Variable, Collection<Token>> firsts = new HashMap<>();
        // Every token maps to itself (as a singleton)
        terminals.forEach((t) -> {
            firsts.put(t, new SingletonCollection<>(t));
        });
        // Initialize the lists for all non-terminals
        nonterminals.stream().forEach((n) -> {
            firsts.put(n, new LinkedHashSet<>());
        });
        // The first lists are build up. The process is repeated as long as there
        // are changes to the lists.
        boolean change = true;
        while (change) {
            change = false;
            for (Rule rule : rules) {
                // Consider any rule and figure out all tokens that might possible
                // belong to the firsts
                Collection<Token> additions = new HashSet<>();
                // Study the first variable on the right hand side of the rule
                Variable[] vs = rule.getRhs();
                if (vs.length == 0) {
                    // The rule derives to the empty word
                    additions.add(EPSILON);
                } else {
                    // There is a variable. Add all firsts of that variable except
                    // the empty word token
                    additions.addAll(firsts.get(vs[0]));
                    additions.remove(EPSILON);
                }
                // As long as the variables of the right hand side can derive to
                // the empty word, the following variables have to be considered
                // as well.
                for (int i = 0; i < vs.length - 1; i++) {
                    if (firsts.get(vs[i]).contains(EPSILON)) {
                        additions.addAll(firsts.get(vs[i + 1]));
                        additions.remove(EPSILON);
                    } else {
                        break;
                    }
                    if (i == vs.length - 2 && firsts.get(vs[i + 1]).contains(EPSILON)) {
                        additions.add(EPSILON);
                    }
                }
                // Add all new possible firsts and see if this results in a change
                change |= firsts.get(rule.getLhs()).addAll(additions);
            }
        }
        return firsts;
    }

    /*
     * Computes the list of those tokens that might appear first when deriving 
     * given rules. The empty word token is possible as well.
     */
    private static Collection<Token> firstOfRule(Rule rule, Map<Variable, Collection<Token>> firsts) {
        Variable[] vs = rule.getRhs();
        if (vs.length == 0) {
            // The rule derives to the empty word
            return new SingletonCollection<>(EPSILON);
        }
        // Consider the firsts of the first variable of the right hand side
        Collection<Token> firstOfRule = new LinkedHashSet<>(firsts.get(vs[0]));
        if (vs.length > 1) {
            firstOfRule.remove(EPSILON);
        }
        // As long as the variables of the right hand side can derive to the 
        // empty word, the following variables have to be considered as well.
        for (int i = 0; i < vs.length - 1; i++) {
            if (firsts.get(vs[i]).contains(EPSILON)) {
                firstOfRule.addAll(firsts.get(vs[i + 1]));
                firstOfRule.remove(EPSILON);
            } else {
                break;
            }
            if (i == vs.length - 2 && firsts.get(vs[i + 1]).contains(EPSILON)) {
                firstOfRule.add(EPSILON);
            }
        }
        return firstOfRule;
    }

    /*
     * The follows map assigns to every non-terminal a list of tokens. It is the
     * list of those tokens that might appear first after a derivation of the
     * non-terminal acording to the set of rules. The empty word token is possible
     * as well.
     */
    private static Map<NonTerminal, Collection<Token>> follows(
            Collection<Rule> rules,
            Map<Variable, Collection<Token>> firsts,
            Collection<NonTerminal> nonterminals,
            NonTerminal start
    ) {
        Map<NonTerminal, Collection<Token>> follows = new HashMap<>();
        // initialize the follow lists
        nonterminals.stream().forEach((n) -> {
            Collection<Token> follow = new LinkedHashSet<>();
            // the start symbol must have EOF as a follow token
            if (start.equals(n)) {
                follow.add(Token.EOF);
            }
            follows.put(n, follow);
        });
        // The follow lists are build up. The process is repeated as long as there
        // are changes to the lists.
        boolean change = true;
        while (change) {
            change = false;
            for (Rule rule : rules) {
                // Consider each rule and its right hand side
                Variable[] vs = rule.getRhs();
                for (int i = 0; i < vs.length; i++) {
                    // Only the non-terminals are of interest here
                    if (vs[i] instanceof NonTerminal) {
                        NonTerminal n = (NonTerminal) vs[i];
                        // The array c consists of all variables after the current
                        // non-terminal n
                        Variable[] c = new Variable[vs.length - i - 1];
                        Collection<Token> newfollow = new LinkedHashSet<>();
                        System.arraycopy(vs, i + 1, c, 0, c.length);
                        // Compute the firsts of that array...
                        Collection<Token> firstOfC = firstOfRule(new Rule(n, c), firsts);
                        // ...and add them as possible new follows
                        if (c.length > 0) {
                            newfollow.addAll(firstOfC);
                            newfollow.remove(EPSILON);
                        }
                        if (c.length == 0 || firstOfC.contains(EPSILON)) {
                            newfollow.addAll(follows.get(rule.getLhs()));
                        }
                        // Add all new possible follows and see if this results
                        // in a change
                        change |= follows.get(n).addAll(newfollow);
                    }
                }
            }
        }
        return follows;
    }

    /*
     * The first+ sets are crucial for the generation of the parsing table.
     * The first+ sets can be directly computed from the firsts and follows.
     */
    private static Collection<Token> firstplusOfRule(Rule rule,
            Map<Variable, Collection<Token>> firsts,
            Map<NonTerminal, Collection<Token>> follows) {
        // the set consists of all firsts...
        Collection<Token> firstplus = new LinkedHashSet<>(firstOfRule(rule, firsts));
        // and all follows if the firsts contain the empty word token
        if (firstplus.contains(EPSILON)) {
            firstplus.addAll(follows.get(rule.getLhs()));
        }
        return firstplus;
    }

    /**
     * This method tries to convert the given context free grammar into an
     * LL(1)-grammar and generates a parsing table for that. For the
     * transformation this method first eliminates all recursions appearing in
     * the grammar and then extracts common beginnings of rules. I do not
     * guarantee that every grammar of an LL(1) language will successfully
     * generate a parsing table through this method.
     *
     * @param ruleSet the rules of the grammar
     * @param start the start symbol of the grammar
     * @return the parsing table for the grammar if it can be transformed into
     * an LL(1)-grammar
     */
    public static ParsingTable generateParsingTable(RuleSet ruleSet, NonTerminal start) {
        // Transform the grammar into an LL(1)-grammar...
        Collection<Rule> rules = toLL1(eliminateRecursion(ruleSet.getRules()));
        // ...and generate the parsing table from that
        return generateParsingTable(rules, extractNonTerminals(rules), extractTerminals(rules), start);
    }

    /*
     * For an LL(1)-grammar this method generates the according parsing table by
     * computing the first+ sets of the rules.
     */
    private static ParsingTable generateParsingTable(
            Collection<Rule> rules,
            Collection<NonTerminal> nonTerminals,
            Collection<Token> terminals,
            NonTerminal start
    ) {
        // Determine the first and follow sets
        Map<Variable, Collection<Token>> firsts = firsts(rules, nonTerminals, terminals);
        Map<NonTerminal, Collection<Token>> follows = follows(rules, firsts, nonTerminals, start);
        // Build the parsing table
        ParsingTable table = new ParsingTable();
        rules.stream().forEach((rule) -> {
            // Each token in the first+ set of a rule determines the rule together
            // with its left hand side
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

    /*
     * The rule tree is used to determine common beginnings of the right hand
     * sides of rules. All rules create branches in the tree. Two branches agree
     * for so long as the according rules consist of the same variables.
     */
    private static class RuleTree {

        private final Map<Variable, RuleTree> children;
        // The rule can be null. It is only non-null if the path from the root
        // indeed forms the input string of the token
        private Rule rule;

        public RuleTree() {
            children = new HashMap<>();
        }

        /*
         * Insert a new rule. This is a recursive method. It has to be initially
         * called with the right hand side of the rule as second parameter.
         */
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

    /**
     * A rewrite rule is a special type of rule used to transform a context-free
     * grammar into an LL(1)-grammar. The part of the transformation where these
     * rules come in is the elimination of common beginnings of the right hand
     * side of rules. The common beginnings are extracted to a new rule that
     * branches to realize the original rules. To achieve this a rule tree is
     * used. In the rule tree every branch corresponds to an original rule. The
     * tree can then be decomposed into its edges which creates many new rules.
     * At the end of every branch a rewrite rule is created which remembers the
     * original rule that created the branch.
     */
    public static class RewriteRule extends Rule {

        private final Rule originalRule;

        /**
         * Create a new rewrite rule.
         *
         * @param originalRule the original rule
         * @param lhs the left hand side of the rewrite rule
         * @param rhs the right hand side of the rewrite rule
         */
        public RewriteRule(Rule originalRule, NonTerminal lhs, Variable... rhs) {
            super(lhs, rhs);
            this.originalRule = originalRule;
        }

        /**
         * @return the original rule
         */
        public Rule getOriginalRule() {
            return originalRule;
        }

        @Override
        public String toString() {
            return super.toString() + " rewrites " + originalRule;
        }

    }

    /**
     * A recursion rule is a special type of rule used to transform a
     * context-free grammar into an LL(1)-grammar. The part of the
     * transformation where these rules come in is the elimination of
     * left-recursions, i.e. rules whose right hand sind beginn with their left
     * hand side. All rules that have this left hand side have to be
     * transformed. The transformed rules are instances of this class.
     */
    public static class RecursionRule extends Rule {

        private final Rule originalRule;
        private final boolean head;

        /**
         * Create a recursion rule, i.e a rule used to resolve a left recursion.
         *
         * @param originalRule the original rule
         * @param head does this rule come from the original rule causing the
         * recursion (and is not the recursion end)
         * @param lhs left hand side of the recursion rule
         * @param rhs right hand side of the recursion rule
         */
        public RecursionRule(Rule originalRule, boolean head, NonTerminal lhs, Variable... rhs) {
            super(lhs, rhs);
            this.originalRule = originalRule;
            this.head = head;
        }

        /**
         * @return the original rule
         */
        public Rule getOriginalRule() {
            return originalRule;
        }

        /**
         * @return Does this rule come from the original rule causing the
         * recursion (and is not the recursion end).
         */
        public boolean isHead() {
            return head;
        }

        @Override
        public String toString() {
            return super.toString() + (originalRule == null ? " recursion end" : (head ? " head for " : " rewrites ") + originalRule);
        }

    }

}
