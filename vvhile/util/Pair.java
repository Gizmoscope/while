package vvhile.util;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of two objects.This object provides
 * a sensible implementation of equals(), returning true if equals() is true on
 * each of the contained objects.
 *
 * @param <F> Type of first component
 * @param <S> Type of second component
 */
public class Pair<F, S> {

    private final F first;
    private final S second;

    /**
     * Constructor for a Pair.
     *
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Pair} to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     * equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }

    /**
     * @return first component
     */
    public F first() {
        return first;
    }

    /**
     * @return second component
     */
    public S second() {
        return second;
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the Pair
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }

}
