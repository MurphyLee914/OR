package Dijkstra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;

public class EdgeWeightedDigraph {
    private final int vertexNum;
    private int edgeNum;
    private LinkedList<DirectedEdge>[] adj;

    @SuppressWarnings("unchecked")
    public EdgeWeightedDigraph(int vertexNum) {
        if  (vertexNum < 0) {
            throw new IllegalArgumentException("vertexNum must be non-negative");
        }
        this.vertexNum = vertexNum;

        edgeNum = 0;
        adj = new LinkedList[vertexNum];
        for (int v = 0; v < vertexNum; v++) {
            adj[v] = new LinkedList<>();
        }
    }

    public EdgeWeightedDigraph(EdgeWeightedDigraph g) {
        this(g.vertexNum);
        this.edgeNum = g.edgeNum;

        for (int v = 0; v < vertexNum; v++) {
            Deque<DirectedEdge> reverse = new ArrayDeque<>();
            for (DirectedEdge e : g.adj[v]) {
                reverse.push(e);
            }

            for (DirectedEdge e : reverse) {
                adj[v].push(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public EdgeWeightedDigraph(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(filename));

        vertexNum = scanner.nextInt();
        adj = new LinkedList[vertexNum];
        for (int v = 0; v < vertexNum; v++) {
            adj[v] = new LinkedList<>();
        }

        int n = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            int from = scanner.nextInt();
            int to = scanner.nextInt();
            double weight = scanner.nextDouble();
            addEdge(new DirectedEdge(from, to, weight));
        }

        scanner.close();
    }

    public void addEdge(DirectedEdge e) {
        int from = e.from();
        int to = e.to();
        validateVertex(from);
        validateVertex(to);

        adj[from].push(e);
        edgeNum++;
    }

    public Iterable<DirectedEdge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    public Iterable<DirectedEdge> edges() {
        LinkedList<DirectedEdge> list = new LinkedList<>();
        for (int v = 0; v < vertexNum; v++) {
            for (DirectedEdge e : adj[v]) {
                list.push(e);
            }
        }

        return list;
    }

    public int getVertexNum() {
        return vertexNum;
    }

    public int getEdgeNum() {
        return edgeNum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vertexNum + " " + edgeNum + "\n");
        for (int v = 0; v < vertexNum; v++) {
            sb.append(v + ": ");
            for (DirectedEdge e : adj[v]) {
                sb.append(e + " ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= vertexNum) {
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (vertexNum - 1));
        }
    }

}
