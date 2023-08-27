package Maxflow;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class FordFulkerson {
	private static final double EPS = 1e-10;
	/** marked[v] = true iff s->v path in residual graph. */
	private boolean[] marked;
	/** edgeTo [v] = last edge on shortest residual s->v path. */
	private FlowEdge[] edgeTo;
	/** current value of max flow. */
	private double value;

    /**
	* Compute a max flow and min cut in the network g from vertex s to t: ‹br>
	* The key is increasing flow along augmenting paths.
	* @param g the flow network
	* @param s the source vertex
	* @param t the sink vertex
	*/
	public FordFulkerson (FlowNetwork g, int s, int t) {
		validate (s, g.getVertexNum());
		validate (t, g.getVertexNum());
		
		if (s == t) {
			throw new IllegalArgumentException ("Source equals sink");
		}

		if (!isFeasible (g, s, t)) {
			throw new IllegalArgumentException ("Initial flow is infeasible");
		}
		// while there exists an augmenting path, use it (the value)
		value = excess (g, s);
		while (hasAugmentingPath(g, s, t)) {
			// 1 compute bottleneck capacity
			double bottleneck = Double.POSITIVE_INFINITY;
			for (int v = t; v != s; v = edgeTo[v].other (v)) {
				bottleneck = Math.min (bottleneck, edgeTo[v].residualCapacityTo(v));
			}

			// 2 update the flow on the augment path
			for (int v = t; v != s; v = edgeTo[v].other (v)) {
				edgeTo[v] .addResidualFlowTo(v, bottleneck);
			}

			// 3 update current "max-flow"
			value += bottleneck;
		}
		// If there is no augmenting path, then the max-flow/min-cut is found.
	}

	/**
	* is v in the s side of the min s-t cut?/is v reachable from s in residual network?.
	* @param v the vertex
	* @return true ij vertex is on the side of mincut, false otherwise
	*/
	public boolean inCut(int v) {
		validate (v, marked.length);
		return marked[v];
	}

	/**
	* Return the value of the max flow.
	* @return the value of the max flow
	*/
	public double value() {
		return value;
	}

	/**
	* Is there an augmenting path? <br>
	* if so, upon termination edgeTo [] will contain a parent-link representation of such a path <br›
	* this implementation finds a shortest augmenting path (fewest number of edges), ‹br>
	* which performs well both in theory and in practice <br>
	* The augmenting path: <br>
	* 1 can increase flow on forward edges (not full) <br>
	* 2 can decrease flow on backward edge (not empty).
	* @param g the flow network
	* @param s the source vertex
	* @param t the sink vertex
	* @return True if there is an augmenting path, false otherwise
	*/
	private boolean hasAugmentingPath (FlowNetwork g, int s, int t) {
		int vertexNum = g.getVertexNum();
		edgeTo = new FlowEdge[vertexNum];
		marked = new boolean[vertexNum];
		
		// breadth-first search
		Queue<Integer> queue = new LinkedList<>();
		queue.add(s) ;
		marked[s] = true;
		while (!queue.isEmpty () && !marked[t]) {
			int v = queue.poll();
			
			for (FlowEdge e: g.adj (v)) {
				int w = e.other (v);
				if (e.residualCapacityTo(w) > 0) {
					if (!marked[w]) {
						edgeTo[w] = e;
						marked[w] = true;
						queue.add(w);
					}
				}
			}
		}
		
		// Is there an augmenting path?
		return marked [t];
	}

	private boolean isFeasible (FlowNetwork g, int s, int t) {
		// check that capacity constraints are satisfied
		int vertexNum = g.getVertexNum() ;
		for (int v = 0; v < vertexNum; v++) {
			for (FlowEdge e : g.adj(v)) {
				if (e.getFlow() < -EPS || e.getFlow() > e.getCapacity()) {
					System.err.println("Edge does not satisfy capacity constraints: " + e);
					return false;
				}
			}
		}

		// check that net flow into a vertex equals zero, except at source and sink
		if (Math.abs (value + excess (g, s)) > EPS) {
			System.err.println("Excess at source = " + excess(g, s));
			System.err.println("Max flow  = " + value);
			return false;
		}
		if (Math.abs (value - excess (g, t)) > EPS) {
			System.err.println("Excess at sink = " + excess(g, t));
			System.err.println("Max flow  " + value);
			return false;
		}
		for (int v = 0; v < vertexNum; v++) {
			if (v == s || v == t) {
			continue;
		}

		if (Math.abs(excess (g, v))> EPS) {
			return false;
		}
	}

		return true;
	}

	/**
	* Return excess flow(in flow - out flow) at vertex v.
	*
	* Oparam g the flow network
	* param v the verter
	* return excess flow at verter v
	*/
	private double excess (FlowNetwork g, int v){
		double excess = 0.0;
		for (FlowEdge e : g.adj(v)) {
			if (v == e.from()) {
				// out-flow
				excess -= e.getFlow();
			} else {
				// in-flow
				excess += e.getFlow();
			}
		}

		return excess;
	}

	private void validate(int v, int n){
		if (v < 0 || v >=n){
			throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (n - 1));
		}
	}

	public static void main(String[] args) {
		// filename of the graph
		String filename = "./data/tinyFN.txt";

		FlowNetwork g = null;
		try {
			g = new FlowNetwork(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int s = 0;
		int vertexNum = g.getVertexNum();
		int t = vertexNum - 1;
		FordFulkerson maxFlow = new FordFulkerson (g, s, t);

		StringBuilder sb = new StringBuilder (String.format ("Max flow from % to %d:\n", s, t));
		for (int v = 0; v < vertexNum; v++) {
			for (FlowEdge e: g.adj (v)) {
				if ((v == e.from()) && e.getFlow() > 0) {
					sb. append(e);
					sb.append('\n');
				}
			}
		}
		// print min-cut
		sb.append("Min cut: ");
		for (int v = 0; v < vertexNum; v++) {
			if (maxFlow.inCut(v)) {
				sb.append(v + " ");
			}
		}
		sb.append("\n");
		sb.append("Max flow value = " + maxFlow. value ());

		System.out.print (sb.toString());
	}
}