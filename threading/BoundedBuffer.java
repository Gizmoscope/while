/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threading;

/**
 *
 * @author markus
 * @param <T>
 */
public class BoundedBuffer<T> {

    private final Object[] buf;
    int in = 0;
    int out = 0;
    int count = 0;
    int size = 0;
    
    public BoundedBuffer(int size) {
        this.size = size;
        buf = new Object[size];
    }

    public synchronized void put(T o) {
        while (count == size) { // buffer full
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        buf[in] = o;
        in = (in + 1) % size;
        count++;
        notifyAll();
    }

    public synchronized T get() {
        while (count == 0) { // buffer empty
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        T o = (T) buf[out];
        buf[out] = null; 
        out = (out + 1) % size;
        count--; 
        notifyAll();
        return o;
    }
}
