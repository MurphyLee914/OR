package Maxflow;


public class FlowEdge {
	private final int from;
	private final int to;
	private final double capacity;
	private double flow;

	public FlowEdge (int from, int to, double capacity) {
		if (from < 0 || to < 0) {
			throw new IndexOutOfBoundsException ("Vertex name must be a nonnegative integer");
		}
		if (capacity < 0.0) {
			throw new IllegalArgumentException ("Edge capacity must be nonnegaitve");
		}

		this.from = from;
		this.to = to;
		this.capacity = capacity;
		this.flow = 0.0;
	}

	public FlowEdge (int from, int to, double capacity, double flow) {
		if (from < 0 || to < 0) {
			throw new IndexOutOfBoundsException("Vertex name must be a nonnegative integer");
		}
		if (capacity < 0.0) {
			throw new IllegalArgumentException("Edge capacity must be nonnegaitve");
		}

		if (flow < 0.0) {
			throw new IllegalArgumentException ("Flow must be nonnegative");
		}
		if (flow > capacity) {
			throw new IllegalArgumentException ("Flow exceeds capacity");
		}

		this.from = from;
		this.to = to;
		this.capacity = capacity;
		this.flow = flow;
	}

	public FlowEdge (FlowEdge e) {
		this.from = e.from;
		this.to = e.to;
		this.capacity = e.capacity;
		this.flow = e.flow;
	}
	/**
	 * Return the residual capacity of the edge in the direction to the given vertex: ‹br>
	 * if vertex is the head vertex, the residual capacity equals {@link #flow} <br>
	 * if vertex is the tail vertex, the residual capacity equals {@link #capacity} - {@link #flow}.
	 * @param vertex one endpoint of the edge
	 * @return the residual capacity of the edge in the direction to the given verter
	 */
	 public double residualCapacityTo(int vertex){
		if (vertex == from) {
			return flow;
		} else if (vertex == to) {
			return capacity - flow;
		} else {
			throw new IllegalArgumentException("Illegal endpoint");
		}
	 }
	/**
	* Increases the flow on the edge in the direction to the given verter: <br›
	* if vertex is the head vertex, this decreases the flow on the edge by delta <br>
	* if vertex is the tail vertex, this increases the flow on the edge by delta.
	* Oparam vertex one endpoint of the edge
	 */
	public void addResidualFlowTo(int vertex, double delta){
		if (vertex == from) {
			flow -= delta;
		} else if (vertex == to) {
			flow += delta;
		} else {
			throw new IllegalArgumentException ("Illegal endpoint");
		}

		if (flow < 0.0) {
			throw new IllegalArgumentException("Flow must be nonnegative");
		}
		if (flow > capacity) {
			throw new IllegalArgumentException ("Flow exceeds capacity");
		}
	}

	public int other (int vertex) {
		if (vertex == from) {
			return to;
		} else if (vertex == to) {
			return from;
		} else {
			throw new IllegalArgumentException("Illegal endpoint");
		}
	}

	public int from() {
		return from;
	}

	public int to() {
		return to;
	}

	public double getCapacity () {
		return capacity;
	}

	public double getFlow() {
		return flow;
	}

	@Override
	public String toString() {
		return from + "-›" + to + " " + flow + "/" + capacity;
	}
}