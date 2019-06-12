package vvhile.basic;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author markus
 */
public class Rule {

    private final NonTerminal lhs;
    private final Variable[] rhs;

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

    public NonTerminal getLhs() {
        return lhs;
    }

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

}
