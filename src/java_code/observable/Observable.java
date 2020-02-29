package java_code.observable;

import java.util.ArrayList;

/**
 * Allows for making Observable variables active listeners with a subscribe method.
 * This will be used for the server.
 * @param <E>
 */
public class Observable<E> {
    private ArrayList<Listener<E>> listeners;
    private E object;

    /**
     * Constructor
     */
    public Observable() {
        this.listeners = new ArrayList<>();
    }

    /**
     * Adds a listener to this object.
     *
     * @param listener
     */
    public void subscribe(Listener<E> listener) {
        if (object != null) {
            listener.update(object);
        }
        listeners.add(listener);
    }

    /**
     * Sets this object and notifies all listeners.
     *
     * @param object
     */
    public void set(E object) {
        this.object = object;
        this.listeners.forEach(eListener -> eListener.update(object));
    }

    /**
     * Returns this object.
     *
     * @return
     */
    public E get() {
        return object;
    }
}