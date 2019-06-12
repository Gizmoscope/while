package vvhile.basic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import vvhile.basic.Pair;

/**
 *
 * @author markus
 */
public class ParsingTable {

    private final Map<Pair<NonTerminal, Token>, Rule> rules;

    public ParsingTable() {
        rules = new HashMap<>();
    }

    public void addRule(NonTerminal nonTerminal, Token token, Rule rule) {
        Pair p = new Pair<NonTerminal, Token>(nonTerminal, token);
        if (rules.containsKey(p)) {
            throw new ParseException("Grammar is not LL(1). "
                    + "Doublicate Rule for pair (" + nonTerminal + ",\"" + token + "\"). Rules  " + rules.get(p) + "  and  " + rule);
        } else {
            rules.put(p, rule);
        }
    }

    public Rule getRule(NonTerminal nonTerminal, Token token) {
        return rules.get(new Pair<>(nonTerminal, token));
    }

    public List<Token> getPossibleTokensFor(NonTerminal nonTerminal) {
        List<Token> possibles = new LinkedList<>();
        rules.keySet().stream().filter((pair)
                -> (nonTerminal.equals(pair.getKey()))).forEach((pair) -> {
            possibles.add(pair.getValue());
        });
        return possibles;
    }

    private Map<Rule, List<Token>> getLines() {
        Map<Rule, List<Token>> lines = new HashMap<>();
        for (Pair<NonTerminal, Token> pair : rules.keySet()) {
            Rule rule = rules.get(pair);
            if (lines.containsKey(rule)) {
                lines.get(rule).add(pair.getValue());
            } else {
                List<Token> first_plus = new LinkedList<>();
                first_plus.add(pair.getValue());
                lines.put(rule, first_plus);
            }
        }
        return lines;
    }

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
