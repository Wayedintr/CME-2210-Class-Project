package graph;

import java.util.Iterator;

// Interface representing a vertex in a graph.
public interface VertexInterface<T, U> {
    // Get the data stored in the vertex.
    T getData();

    // Set the data stored in the vertex.
    void setData(T data);

    // Mark the vertex as visited.
    void visit();

    // Mark the vertex as unvisited.
    void unvisit();

    // Check if the vertex has been visited.
    boolean isVisited();

    // Connect this vertex to another vertex with an edge containing the given data.
    boolean connect(VertexInterface<T, U> endVertex, U data);

    // Remove an edge between this vertex and another vertex, identified by their data.
    boolean removeEdge(T data, U data2);

    // Remove all edges connecting this vertex to another vertex identified by its data.
    boolean removeAllEdgesTo(T data);

    // Check if there exists an edge between this vertex and another vertex identified by their data.
    boolean hasEdge(T data, U data2);

    // Get the count of edges incident to this vertex.
    int edgeCount();

    // Get an iterator over the neighbors of this vertex.
    Iterator<VertexInterface<T, U>> getNeighborIterator();

    // Get an iterator over the data associated with edges incident to this vertex.
    Iterator<U> getEdgeDataIterator();

    // Check if this vertex has any neighbors.
    boolean hasNeighbor();

    // Get an unvisited neighbor of this vertex if it exists.
    VertexInterface<T, U> getUnvisitedNeighbor();

    // Check if this vertex has any unvisited neighbors.
    boolean hasUnvisitedNeighbor();

    // Set the predecessor vertex of this vertex in a traversal.
    void setPredecessor(VertexInterface<T, U> predecessor);

    // Get the predecessor vertex of this vertex in a traversal.
    VertexInterface<T, U> getPredecessor();

    // Check if this vertex has a predecessor vertex.
    boolean hasPredecessor();

    // Set the data associated with the edge to the predecessor of this vertex.
    void setPredecessorEdgeData(U data);

    // Get the data associated with the edge to the predecessor of this vertex.
    U getPredecessorEdgeData();
}
