package Maxflow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * The FlowNetwork class, representing a capacitated network with vertexes, <br>
 * and directed flow edge {@link FlowEdge} with a real-valued capacity and flow.
 */
public class FlowNetwork {
	private final int vertexNum;
	private int edgeNum;
	private LinkedList<FlowEdge>[] adj;

	@SuppressWarnings ("unchecked")
	public FlowNetwork(int vertexNum) {
		if (vertexNum < 0) {
			throw new IllegalArgumentException("Number of vertices in a Digraph must be nonnegative");
		}
		this.vertexNum = vertexNum;

		edgeNum = 0;
		adj = new LinkedList[vertexNum];
		for (int v = 0; v < vertexNum; v++) {
			adj[v] = new LinkedList<>();
		}
	}

	@SuppressWarnings ("unchecked")
	public FlowNetwork (String filename) throws FileNotFoundException {
		Scanner scanner = new Scanner (new FileInputStream(filename));

		//前两行为节点数和边数
		vertexNum = scanner.nextInt();
		adj = new LinkedList [vertexNum];
		for (int i = 0; i < vertexNum; i++) {
			adj[i] = new LinkedList<>();
		}

		int n = scanner.nextInt();
		for (int i = 0; i < n; i++) {
			int from = scanner.nextInt();
			int to = scanner.nextInt();
			double capacity = scanner.nextDouble();
			addEdge (new FlowEdge (from, to, capacity));
		}
		scanner. close();
	}

	public void addEdge (FlowEdge e) {
		int v = e.from();
		int w = e.to();
		validateVertex (v);
		validateVertex (w);

		adj [v].push(e);
		adj [w].push(e);
		edgeNum++;
	}

	public Iterable<FlowEdge> edges() {
		LinkedList<FlowEdge> list = new LinkedList<>();
		for (int v = 0; v < vertexNum; v++) {
			for (FlowEdge e : adj[v]) {
				if (e.to() != v) {
					list.push(e);
				}
			}
		}

		return list;
	}

	public Iterable<FlowEdge> adj(int v) {
		validateVertex (v);
		return adj[v];
	}

	/** adj[v] = adjacency list for vertex v. */
	public int getVertexNum () {
		return vertexNum;
	}

	public int getEdgeNum() {
		return edgeNum;
	}

	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder ();
 		sb.append (vertexNum + " vertices, " + edgeNum + " edges \n");
 		for (int v = 0; v < vertexNum; v++) {
 			sb.append (v + ": ");
 			for (FlowEdge e : adj[v]){
				if (e.to() != v) {
					sb.append(e + " ");
				}
			}
			sb.append('\n');
		}

		 return sb.toString();
	}

	/**
	 * throw an IllegalArgumentException unless 0 <= v < V.
	 *
	 * @param v the given integer
	 */
	private void validateVertex(int v) {
		if (v < 0 || v >= vertexNum){
			throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (vertexNum - 1));
		}
	}
}