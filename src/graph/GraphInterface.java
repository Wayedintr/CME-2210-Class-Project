package graph;

public interface GraphInterface<T, U> {
    void add(T vertex);

    void connect(T from, T to, U data);

    boolean disconnect(T from, T to, U data);

    boolean contains(T vertex);

    boolean remove(T vertex);

    int size();
}
