package vvhile.frontend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import vvhile.util.Pair;

/**
 * A parsing table assigns to every pair of a non-terminal and a token a rule.
 * This rule
 *
 * @author markus
 */
public class ParsingTable {

    // A non-terminal and a token uniquely determine a rule that applies if the
    // token is read while being at the non-terminal
    private final Map<Pair<NonTerminal, Token>, Rule> rules;

    /**
     * Creates a new parsing table.
     */
    public ParsingTable() {
        rules = new HashMap<>();
    }

    /**
     * Add a new rule to the parsing table. Row and column of the rule are given
     * by the non-terminal and the token.
     *
     * @param nonTerminal row of rule
     * @param token column of rule
     * @param rule the rule
     */
    public void addRule(NonTerminal nonTerminal, Token token, Rule rule) {
        Pair p = new Pair<>(nonTerminal, token);
        if (rules.containsKey(p)) {
            // The pair of non-terminal and token must be unique
            throw new ParseException("Grammar is not LL(1). "
                    + "Doublicate Rule for pair (" + nonTerminal + ",\"" + token + "\"). Rules  " + rules.get(p) + "  and  " + rule);
        } else {
            rules.put(p, rule);
        }
    }

    /**
     * Returns the rule given by the non-terminal and the token.
     *
     * @param nonTerminal row of rule
     * @param token column of rule
     * @return a rule (if exists)
     */
    public Rule getRule(NonTerminal nonTerminal, Token token) {
        Pair<NonTerminal, Token> pair = new Pair<>(nonTerminal, token);
        return rules.get(pair);
    }

    /**
     * Returns a list with all tokens that share a rule with the given
     * non-terminal.
     *
     * @param nonTerminal a non-terminal
     * @return list of related tokens
     */
    public List<Token> getPossibleTokensFor(NonTerminal nonTerminal) {
        List<Token> possibles = new LinkedList<>();
        rules.keySet().stream().filter(
                // filter all row-column-pairs if their first coordinate is the
                // given non-terminal
                (pair) -> nonTerminal.equals(pair.first())
        ).forEach((pair) -> {
            // add the according second coordinate to the list
            possibles.add(pair.second());
        });
        return possibles;
    }

    /*
     * Creates for each rule a list of tokens, the so called first+ list of
     * tokens that trigger the rule. This method is used by the toString()-method.
     */
    private Map<Rule, List<Token>> getLines() {
        Map<Rule, List<Token>> lines = new HashMap<>();
        rules.keySet().forEach((pair) -> {
            Rule rule = rules.get(pair);
            // rules usually appear more that once in the "rules map"
            if (lines.containsKey(rule)) {
                // add the appearing token (it belongs to the first+ list)
                lines.get(rule).add(pair.second());
            } else {
                // create a new list and add the token
                List<Token> first_plus = new LinkedList<>();
                first_plus.add(pair.second());
                lines.put(rule, first_plus);
            }
        });
        return lines;
    }

    /**
     * @return all rules and their triggering tokens aka their first+ lists
     */
    @Override
    public String toString() {
        Map<Rule, List<Token>> lines = getLines();
        StringBuilder builder = new StringBuilder();
        lines.keySet().stream().forEach((rule) -> {
            builder.append(rule).append('\t').append(lines.get(rule)).append('\n');
        });
        return builder.toString();
    }

}
