package graph;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class DirectedGraph<T, U> implements GraphInterface<T, U> {

    private final HashMap<T, VertexInterface<T, U>> vertices;

    public DirectedGraph() {
        vertices = new HashMap<>();
    }

    public void add(T data) {
        VertexInterface<T, U> v = new Vertex<>(data);

        if (!contains(data))
            vertices.put(data, v);
        else
            throw new IllegalArgumentException("Vertex already exists");
    }

    public void connect(T from, T to, U data) {
        VertexInterface<T, U> v1 = vertices.get(from);
        VertexInterface<T, U> v2 = vertices.get(to);

        if (v1 != null && v2 != null)
            v1.connect(v2, data);
        else
            throw new NoSuchElementException("Vertex does not exist");
    }

    public boolean disconnect(T from, T to, U data) {
        VertexInterface<T, U> v1 = vertices.get(from);
        VertexInterface<T, U> v2 = vertices.get(to);

        if (v1 != null && v2 != null)
            return v1.removeEdge(to, data);
        else
            throw new NoSuchElementException("Vertex does not exist");
    }

    public boolean contains(T data) {
        return vertices.containsKey(data);
    }

    public T get(T data) {
        return vertices.get(data).getData();
    }

    public boolean remove(T data) {
        if (vertices.remove(data) != null) {
            List<VertexInterface<T, U>> values = vertices.values().stream().toList();
            for (int i = 1; i <= values.size(); i++) {
                VertexInterface<T, U> v = values.get(i);
                v.removeAllEdgesTo(data);
            }
            return true;
        } else
            return false;
    }

    public int size() {
        return vertices.size();
    }
}
