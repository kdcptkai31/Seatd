package java_code.observable;

/**
 * Listener Interface
 * @param <E>
 */
public interface Listener<E> {
    void update(E message);
}
