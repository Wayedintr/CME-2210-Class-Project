package graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Vertex<T, U> implements VertexInterface<T, U> {
    private T data;
    private final ArrayList<Edge> edges;
    private boolean visited;
    private VertexInterface<T, U> predecessor;

    private U predecessorEdgeData;

    public Vertex(T data) {
        this.data = data;
        edges = new ArrayList<>();
        visited = false;
    }

    public boolean connect(VertexInterface<T, U> endVertex, U data) {
        if (!hasEdge(endVertex.getData(), data)) {
            edges.add(new Edge(endVertex, data));
            return true;
        }
        return false;
    }

    public boolean removeEdge(T data, U data2) {
        Iterator<Edge> iterator = edges.iterator();
        while (iterator.hasNext()) {
            Edge next = iterator.next();
            if (next.getVertex().getData().equals(data) && next.getData().equals(data2)) {
                edges.remove(next);
                return true;
            }
        }
        return false;
    }

    public boolean removeAllEdgesTo(T data) {
        Iterator<Edge> iterator = edges.iterator();
        while (iterator.hasNext()) {
            Edge next = iterator.next();
            if (next.getVertex().getData().equals(data)) {
                edges.remove(next);
            }
        }
        return true;
    }

    public boolean hasEdge(T data, U data2) {
        Iterator<Edge> iterator = edges.iterator();
        while (iterator.hasNext()) {
            Edge next = iterator.next();
            if (next.getVertex().getData().equals(data) && next.getData().equals(data2))
                return true;
        }
        return false;
    }

    public int edgeCount() {
        return edges.size();
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void visit() {
        this.visited = true;
    }

    public void unvisit() {
        this.visited = false;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public VertexInterface<T, U> getUnvisitedNeighbor() {
        Iterator<VertexInterface<T, U>> neighbors = getNeighborIterator();

        while (neighbors.hasNext()) {
            VertexInterface<T, U> nextNeighbor = neighbors.next();
            if (!nextNeighbor.isVisited())
                return nextNeighbor;
        }

        return null;
    }

    public void setPredecessor(VertexInterface<T, U> predecessor) {
        this.predecessor = predecessor;
    }

    public VertexInterface<T, U> getPredecessor() {
        return predecessor;
    }

    public boolean hasPredecessor() {
        return predecessor != null;
    }


    public void setPredecessorEdgeData(U data) {
        predecessorEdgeData = data;
    }

    public U getPredecessorEdgeData() {
        return predecessorEdgeData;
    }

    public Iterator<VertexInterface<T, U>> getNeighborIterator() {
        return new NeighborIterator();
    }

    public Iterator<U> getEdgeDataIterator() {
        return new EdgeDataIterator();
    }

    public boolean hasNeighbor() {
        return getNeighborIterator().hasNext();
    }

    public boolean hasUnvisitedNeighbor() {
        return getUnvisitedNeighbor() != null;
    }

    private class NeighborIterator implements Iterator<VertexInterface<T, U>> {
        int edgeIndex;

        private NeighborIterator() {
            edgeIndex = -1;
        }

        public boolean hasNext() {
            return edgeIndex + 1 < edges.size();
        }

        public VertexInterface<T, U> next() {
            if (hasNext()) {
                edgeIndex++;
                return edges.get(edgeIndex).getVertex();
            } else
                throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class EdgeDataIterator implements Iterator<U> {
        int edgeIndex;

        private EdgeDataIterator() {
            edgeIndex = -1;
        }

        public boolean hasNext() {
            return edgeIndex + 1 < edges.size();
        }

        public U next() {
            if (hasNext()) {
                edgeIndex++;
                return edges.get(edgeIndex).getData();
            } else
                throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected class Edge {
        private VertexInterface<T, U> vertex;

        private U data;

        public Edge(VertexInterface<T, U> vertex, U data) {
            this.vertex = vertex;
            this.data = data;
        }

        public Edge(VertexInterface<T, U> vertex, double weight) {
            this(vertex, null);
        }

        public VertexInterface<T, U> getVertex() {
            return vertex;
        }

        public void setVertex(VertexInterface<T, U> vertex) {
            this.vertex = vertex;
        }

        public U getData() {
            return data;
        }

        public void setData(U data) {
            this.data = data;
        }
    }

}
