package Dijkstra;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public class DijkstraSp {
    private double[] distTo;
    private DirectedEdge[] edgeTo;
    private PriorityQueue<Integer> pq;

    public DijkstraSp(EdgeWeightedDigraph g, int s) {
        for (DirectedEdge e : g.edges()){
            if (e.getWeight() < 0) {
                throw new IllegalArgumentException("edge " + e + " has negative weight");
            }
        }

        int vertexNum = g.getVertexNum();
        distTo = new double[vertexNum];
        edgeTo = new DirectedEdge[vertexNum];

        validateVertex(s);

        for (int v = 0; v < vertexNum; v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[s] = 0.0;

        pq = new PriorityQueue<Integer>(vertexNum, (v, w) -> Double.compare(distTo[v], distTo[w]));
        pq.offer(s);
        while (!pq.isEmpty()) {
            int v = pq.poll();
            for (DirectedEdge e : g.adj(v)) {
                relax(e);
            }
        }
    }

    public boolean hasPathTo(int v) {
        validateVertex(v);
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    public Iterable<DirectedEdge> pathTo(int v) {
        validateVertex(v);
        if (!hasPathTo(v)) {
            return null;
        }
        Deque<DirectedEdge> path = new ArrayDeque<>();
        for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from()]) {
            path.push(e);
        }
        return path;
    }

    public double distTo(int v) {
        validateVertex(v);
        return distTo[v];
    }

    private void relax(DirectedEdge e) {
        int v = e.from(), w = e.to();
        if (distTo[w] > distTo[v] + e.getWeight()) {
            distTo[w] = distTo[v] + e.getWeight();
            edgeTo[w] = e;
            if (pq.contains(w)) {
                pq.remove(w);
            }
            pq.offer(w);
        }
    }

    public void validateVertex(int v) {
        int vertexNum = distTo.length;
        if (v < 0 || v >= vertexNum) {
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (vertexNum - 1));
        }
    }

    public static void main(String[] args)  {
        String filename = "tinyEWD.txt";
        int s = 7;
        EdgeWeightedDigraph g = null;
        try {
            g = new EdgeWeightedDigraph(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DijkstraSp sp = new DijkstraSp(g, s);

        int n = g.getVertexNum();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (sp.hasPathTo(i)) {
                sb.append(String.format("%d to %d (%.2f): ", s, i, sp.distTo(i)));
                for (DirectedEdge e : sp.pathTo(i)) {
                    sb.append(e + " ");
                }
                sb.append("\n");
            } else {
                sb.append(String.format("%d to %d no path\n", s, i));
            }
        }
        System.out.println(sb.toString());
    }
}
