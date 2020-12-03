package vvhile.util;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * An object of this class is a collection consisting of exactly one element.
 * 
 * @author markus
 * @param <T> type of the singleton
 */
public class SingletonCollection<T> extends AbstractCollection<T> {
    
    private final T singleton;

    /**
     * Create a new singleton collection from the given element.
     * @param singleton the element to be the singleton of the collection
     */
    public SingletonCollection(T singleton) {
        this.singleton = singleton;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
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
            public T next() {
                return singleton;
            }
        };
    }

    @Override
    public int size() {
        return 1;
    }

}
