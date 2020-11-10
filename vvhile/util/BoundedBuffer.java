package vvhile.util;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A bounded buffer is a synchronized object that stores a certain capacity of
 * data. A bounded buffer is used to connect a producer and a consumer. Usually
 * both the producer and the consumer work asynchronously. The buffer makes the
 * producer wait if the buffer is full and makes the consumer wait if the buffer
 * is empty.
 * 
 * @author markus
 * @param <T> type of data stored in the buffer
 */
public class BoundedBuffer<T> {

    private final Object[] buf;
    int in = 0;
    int out = 0;
    int count = 0;
    int size = 0;
    
    private final Set<ChangeListener> listeners;
    
    /**
     * Creates a bounded buffer with the given capacity.
     * @param size the fixed capacity for this buffer
     */
    public BoundedBuffer(int size) {
        this.size = size;
        this.listeners = new HashSet<>();
        buf = new Object[size];
    }

    /**
     * Adds an object to the buffer. If the buffer is already full then the
     * calling thread must wait. If it is empty then all waiting comsumers are
     * woken up.
     * 
     * @param o an object
     */
    public synchronized void put(T o) {
        while (count == size) { // buffer full ==> wait
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // at this point the buffer cannot be full. Add the object
        buf[in] = o;
        in = (in + 1) % size;
        count++;
        // notify listeners
        listeners.forEach(l -> l.stateChanged(new ChangeEvent(new Pair(o, true))));
        // notify all consumers
        notifyAll();
    }

    /**
     * Returns an object from the buffer. If the buffer is empty then the
     * calling thread must wait. If it is full then all waiting producers are
     * woken up.
     * 
     * @return an object
     */
    public synchronized T get() {
        while (count == 0) { // buffer empty ==> wait
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // at this point the buffer cannot be empty. Retreive the object
        T o = (T) buf[out];
        buf[out] = null; 
        out = (out + 1) % size;
        count--; 
        // notify listeners
        listeners.forEach(l -> l.stateChanged(new ChangeEvent(new Pair(o, false))));
        // notify all producers
        notifyAll();
        return o;
    }

    /**
     * A listener is notified if an object is put into or retreived from the
     * buffer. The listener is supplied with the object and wether it was added
     * or removed.
     * 
     * @param listener a change listener
     */
    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }
}
