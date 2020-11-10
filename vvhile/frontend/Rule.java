package vvhile.frontend;

import java.util.Arrays;
import java.util.Objects;

/**
 * A rule is an assignment S -> A_1 A_2...A_n, of a non-terminal to a sequence of
 * variables (token or non-terminals).
 * 
 * @author markus
 */
public class Rule implements Parser.ParseObject, Comparable<Rule> {

    // Left hand side (the non-terminal)
    private final NonTerminal lhs;
    // Right hand side (list of variables)
    private final Variable[] rhs;

    /**
     * Creates a Rule of the form S -> A_1 A_2...A_n. Here S is a non-terminal
     * and the A_i are variables, i.e. terminals or non-terminals.
     * 
     * @param lhs Left hand side (the non-terminal)
     * @param rhs Right hand side (list of variables)
     */
    public Rule(NonTerminal lhs, Variable... rhs) {
        if (lhs == null) {
            throw new NullPointerException("Left hand side must not be null.");
        }
        this.lhs = lhs;
        if (rhs == null) {
            this.rhs = new Variable[0];
        } else {
            this.rhs = rhs;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(lhs.toString());
        builder.append(" → ");
        // If the right hand side is empty the rule produces the empty word ε.
        if (rhs.length == 0) {
            builder.append('ε');
        } else {
            builder.append(rhs[0]);
            for (int i = 1; i < rhs.length; i++) {
                builder.append(' ').append(rhs[i]);
            }
        }
        return builder.toString();
    }

    /**
     * @return Left hand side (the non-terminal)
     */
    public NonTerminal getLhs() {
        return lhs;
    }

    /**
     * @return Right hand side (list of variables)
     */
    public Variable[] getRhs() {
        return rhs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.lhs);
        hash = 59 * hash + Arrays.deepHashCode(this.rhs);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rule other = (Rule) obj;
        if (!Objects.equals(this.lhs, other.lhs)) {
            return false;
        }
        return Arrays.deepEquals(this.rhs, other.rhs);
    }

    /**
     * Compares rules by there string representation.
     * 
     * @param r another rule
     * @return comparison of string representations
     */
    @Override
    public int compareTo(Rule r) {
        int c = lhs.getName().compareTo(r.lhs.getName());
        if (c != 0) {
            return c;
        }
        for(int i=0; i<rhs.length && i<r.rhs.length && c != 0; i++) {
            c = rhs[i].toString().compareTo(r.rhs[i].toString());
        }
        return c;
    }

}
