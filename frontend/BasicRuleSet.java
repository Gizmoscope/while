package frontend;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import static frontend.Token.*;

/**
 *
 * @author markus
 */
public class BasicRuleSet {

    public static final NonTerminal STM = new NonTerminal("STM");
    public static final NonTerminal STM_ = new NonTerminal("STM'");
    public static final NonTerminal EXPR = new NonTerminal("EXPR");
    public static final NonTerminal EXPR_ = new NonTerminal("EXPR'");
    public static final NonTerminal COMP = new NonTerminal("COMP");
    public static final NonTerminal COMP_ = new NonTerminal("COMP'");
    public static final NonTerminal TERM = new NonTerminal("TERM");
    public static final NonTerminal TERM_ = new NonTerminal("TERM'");
    public static final NonTerminal FACT = new NonTerminal("FACT");
    public static final NonTerminal QUANT = new NonTerminal("QUANT");
    public static final NonTerminal ASSIGN_ = new NonTerminal("ASSIGN'");

    public static final Collection<NonTerminal> BASIS_NONTERMINALS = Arrays.asList(new NonTerminal[]{
        STM, STM_, EXPR, EXPR_, COMP, COMP_, TERM, TERM_, FACT, QUANT, ASSIGN_,
    });
    public static final Collection<Token> BASIC_TERMINALS = Arrays.asList(new Token[]{
        L_PAREN, R_PAREN, SEMICOLON, L_CURLY, R_CURLY, ASSIGN, LESS_THAN,
        LESS_EQUAL, EQUALS, GREATER_EQUAL, GREATER_THAN, PLUS, MINUS, TIMES,
        AND, OR, NOT, ID, NUM, EOF, SKIP, IF, ELSE, WHILE, TRUE, FALSE, UNKNOWN,
        FORALL, EXISTS, DOT, IMPLIES, IMPLIED_BY,
    });

    public static final Collection<Rule> BASIC_RULES = Arrays.asList(new Rule[]{
        // STM
        new Rule(STM, ID, ASSIGN_),
        new Rule(STM, SKIP, STM_),
        new Rule(STM, IF, L_PAREN, EXPR, R_PAREN, L_CURLY, STM, R_CURLY, ELSE, L_CURLY, STM, R_CURLY, STM_),
        new Rule(STM, WHILE, L_PAREN, EXPR, R_PAREN, L_CURLY, STM, R_CURLY, STM_),
        // STM'
        new Rule(STM_, SEMICOLON, STM),
        new Rule(STM_),
        // ASSIGN'
        new Rule(ASSIGN_, ASSIGN, EXPR, STM_),
        new Rule(ASSIGN_, STM_),
        // EXPR
        new Rule(EXPR, COMP, EXPR_),
        // EXPR'
        new Rule(EXPR_, LESS_THAN, EXPR),
        new Rule(EXPR_, LESS_EQUAL, EXPR),
        new Rule(EXPR_, EQUALS, EXPR),
        new Rule(EXPR_, GREATER_EQUAL, EXPR),
        new Rule(EXPR_, GREATER_THAN, EXPR),
        new Rule(EXPR_, IMPLIES, EXPR),
        new Rule(EXPR_, IMPLIED_BY, EXPR),
        new Rule(EXPR_),
        // COMP
        new Rule(COMP, TERM, COMP_),
        //COMP'
        new Rule(COMP_, PLUS, COMP),
        new Rule(COMP_, MINUS, COMP),
        new Rule(COMP_, OR, COMP),
        new Rule(COMP_),
        // TERM
        new Rule(TERM, FACT, TERM_),
        // TERM'
        new Rule(TERM_, TIMES, TERM),
        new Rule(TERM_, AND, TERM),
        new Rule(TERM_),
        // FACT
        new Rule(FACT, NUM),
        new Rule(FACT, TRUE),
        new Rule(FACT, FALSE),
        new Rule(FACT, UNKNOWN),
        new Rule(FACT, ID),
        new Rule(FACT, NOT, FACT),
        new Rule(FACT, MINUS, FACT),
        new Rule(FACT, L_PAREN, QUANT),
        // QUANT
        new Rule(QUANT, EXPR, R_PAREN),
        new Rule(QUANT, FORALL, ID, DOT, EXPR, R_PAREN),
        new Rule(QUANT, EXISTS, ID, DOT, EXPR, R_PAREN),
    });

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
                } else
                    if (!firsts.get(rule.getLhs()).containsAll(rhs)) {
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
            Collection<NonTerminal> nonterminals
    ) {
        Map<NonTerminal, Collection<Token>> follows = new HashMap<>();
        nonterminals.stream().forEach((n) -> {
            Collection<Token> follow = new LinkedHashSet<>();
            if (STM.equals(n)) {
                follow.add(EOF);
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

    public static ParsingTable generateParsingTable(
            Collection<Rule> rules,
            Collection<NonTerminal> nonTerminals,
            Collection<Token> terminals
    ) {
        Map<Variable, Collection<Token>> firsts = firsts(rules, nonTerminals, terminals);
//        for(Variable v: firsts.keyCollection()) {
//            System.out.println("FIRST(" + v + ") = " + firsts.get(v));
//        }
//        System.out.println();
//        for(Rule r: rules) {
//            System.out.println("FIRST(" + r + ") = " + firstOfRule(r, firsts));
//        }
//        System.out.println();
        Map<NonTerminal, Collection<Token>> follows = follows(rules, firsts, nonTerminals);
//        for(NonTerminal n: follows.keyCollection()) {
//            System.out.println("FOLLOW(" + n + ") = " + follows.get(n));
//        }
//        System.out.println();
        ParsingTable table = new ParsingTable();
        rules.stream().forEach((rule) -> {
            Collection<Token> firstplus = firstplusOfRule(rule, firsts, follows);
//            System.out.println("FIRST+(" + rule + ") = " + firstplus);
            firstplus.stream().forEach((t) -> {
                table.addRule(rule.getLhs(), t, rule);
            });
        });
//        System.out.println();
        return table;
    }

    private static final Token EPSILON = new Token() {
        @Override
        public String toString() {
            return "ε";
        }
    };
}
