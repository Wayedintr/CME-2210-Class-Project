package graph;

// Interface representing a generic graph structure.
public interface GraphInterface<T, U> {
    // Add a vertex to the graph.
    void add(T vertex);

    // Connect two vertices in the graph with an edge containing the given data.
    void connect(T from, T to, U data);

    // Disconnect two vertices in the graph if there exists an edge with the given data between them.
    boolean disconnect(T from, T to, U data);

    // Check if the graph contains a specific vertex.
    boolean contains(T vertex);

    // Remove a vertex from the graph along with all its incident edges.
    boolean remove(T vertex);

    // Get the number of vertices in the graph.
    int size();
}