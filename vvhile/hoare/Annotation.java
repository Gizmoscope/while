package vvhile.hoare;

import java.util.Objects;

/**
 * An annotation is a boolean formular that is meant to be glued to a statement.
 * The boolean formular can be labelled as an invariant, e.g. for loops.
 * 
 * @author markus
 */
public class Annotation {

    private final boolean invariant;
    private final BooleanFormula annotation;

    /**
     * Creates a new annotation object.
     * 
     * @param invariant is the boolean formular an invariant
     * @param annotation a boolean formular
     */
    public Annotation(boolean invariant, BooleanFormula annotation) {
        this.invariant = invariant;
        this.annotation = annotation;
    }

    /**
     * @return true if the boolean formular is an invariant
     */
    public boolean isInvariant() {
        return invariant;
    }

    /**
     * @return the actual annotated formular
     */
    public BooleanFormula getAnnotation() {
        return annotation;
    }

    /**
     * @param latex true if the return string should represent LaTeX code
     * @return a string representation
     */
    public String toString(boolean latex) {
        return (latex
                ? (invariant ? "\\{ \\operatorname{inv}: "
                        : "\\{") : (invariant ? "{inv: " : "{"))
                + annotation.toString(latex) + (latex ? "\\}" : "}");
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.invariant ? 1 : 0);
        hash = 11 * hash + Objects.hashCode(this.annotation);
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
        final Annotation other = (Annotation) obj;
        if (this.invariant != other.invariant) {
            return false;
        }
        return Objects.equals(this.annotation, other.annotation);
    }
}
