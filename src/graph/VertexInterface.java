package graph;

import java.util.Iterator;

public interface VertexInterface<T, U> {
    T getData();

    void setData(T data);

    void visit();

    void unvisit();

    boolean isVisited();

    boolean connect(VertexInterface<T, U> endVertex, U data);

    boolean removeEdge(T data, U data2);

    boolean removeAllEdgesTo(T data);

    boolean hasEdge(T data, U data2);

    int edgeCount();

    Iterator<VertexInterface<T, U>> getNeighborIterator();

    Iterator<U> getEdgeDataIterator();

    boolean hasNeighbor();

    VertexInterface<T, U> getUnvisitedNeighbor();

    boolean hasUnvisitedNeighbor();

    void setPredecessor(VertexInterface<T, U> predecessor);

    VertexInterface<T, U> getPredecessor();

    boolean hasPredecessor();

    void setPredecessorEdgeData(U data);

    U getPredecessorEdgeData();
}
