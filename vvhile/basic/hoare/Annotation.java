package vvhile.basic.hoare;

import java.util.Objects;

/**
 *
 * @author markus
 */
public class Annotation {

    private final boolean isInvariant;
    private final BooleanFormula annotation;

    public Annotation(boolean isInvariant, BooleanFormula annotation) {
        this.isInvariant = isInvariant;
        this.annotation = annotation;
    }

    public boolean isIsInvariant() {
        return isInvariant;
    }

    public BooleanFormula getAnnotation() {
        return annotation;
    }

    public String toString(boolean latex) {
        return (latex
                ? (isInvariant ? "\\{ \\operatorname{inv}: "
                        : "\\{") : (isInvariant ? "{inv: " : "{"))
                + annotation.toString(latex) + (latex ? "\\}" : "}");
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.isInvariant ? 1 : 0);
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
        if (this.isInvariant != other.isInvariant) {
            return false;
        }
        return Objects.equals(this.annotation, other.annotation);
    }
}
