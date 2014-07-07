package msdingfield.easyflow.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msdingfield.easyflow.graph.support.CyclicDependencyException;
import msdingfield.easyflow.graph.support.DuplicateOutputsFoundException;
import msdingfield.easyflow.graph.support.NodeNotFoundException;
import msdingfield.easyflow.graph.support.OutputNotFoundException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/** A graph of FlowNodes.
 *
 * A FlowGraph analysis and exposes the dependency structure of a collection
 * of FlowNodes.  A FlowNode instance defines a node of a directed graph by
 * naming input and output vertices.  The FlowGraph connects nodes by matching
 * named inputs with named outputs.
 * 
 * Since it is assumed that each vertex represents a value to be transmitted
 * from a producer to a consumer, it is not allowed for the same name to be
 * applied to more than one node output.  Constructing a FlowGraph with such
 * a set of FlowNodes will throw a DuplicateOutputsFoundException.
 * 
 * Cycles are also not allowed.  Constructing a FlowGraph which contains cycles
 * will throw CyclicDependencyException.
 * 
 * However, it is not required that every output be consumed or that every
 * input receive a value.  Unused outputs are ignored.  Typically, nodes
 * with unsatisfied inputs are also ignored.  The expectation is that the graph
 * will define a superset of the task nodes desired for a single use and so
 * only those required should be evaluated.
 *
 * @author Matt
 *
 */
public class Graph<Node extends GraphNode> {

	/** All nodes in graph. */
	private final Set<Node> allNodes = Sets.newHashSet();

	/**
	 * Map from the named output to producing node.
	 * 
	 * The presence of a name in this map implies the name is the output of
	 * exactly 1 node in the graph.  However, no inference is possible on the
	 * number of nodes consuming the output.
	 */
	private final Map<String, Node> outputNameToNode = Maps.newHashMap();

	/**
	 * Map from named input to all consuming nodes.
	 * 
	 * The presence of a name in this map implies the name is the input of
	 * 1 or more nodes in the graph.  However, no inference is possible on the
	 * number of nodes producing the input.
	 */
	private final Map<String, Set<Node>> inputNameToNodes = Maps.newHashMap();

	/**
	 * Map of a node to its direct predecessor nodes.
	 * 
	 * This is a map from a node to all of its non-transitive predecessors.
	 * That is, there is a directed edge into Node N from each node in the set
	 * directPredecessors.get(N).
	 * 
	 * This implies the following constraint:
	 * 
	 * ND.getInputs().contains(NM) iff exists P in directPredecessors.get(ND)
	 * such that P.getOutputs().contains(NM)
	 */
	private final Map<Node, Set<Node>> directPredecessors = Maps.newHashMap();

	/**
	 * Map of node to its direct successor nodes.
	 * 
	 * This is a map from a node to all of its non-transitive successors.
	 * That is, there is a directed edge from Node N into each node in the set
	 * directSuccessors.get(N).
	 * 
	 * This implies the following constraint:
	 * 
	 * ND.getOutputs().contains(NM) iff exists S in directSuccessors.get(ND)
	 * such that S.getInputs().contains(NM)
	 */
	private final Map<Node, Set<Node>> directSuccessors = Maps.newHashMap();

	/** Create FlowGraph from a set of nodes. */
	public Graph(final Set<Node> nodes) {
		allNodes.addAll(nodes);
		init();
	}

	/** Get all nodes in the graph. */
	public Set<Node> getAllNodes() {
		return Collections.unmodifiableSet(allNodes);
	}

	/** Get all direct predecessors of a node. */
	public Set<Node> getDirectPredecessors(final Node node) {
		assert node != null;
		if (!directSuccessors.containsKey(node)) {
			throw new NodeNotFoundException("Failed to get direct predecessors.  The requested node is not part of the graph.");
		}
		return Collections.unmodifiableSet(directPredecessors.get(node));
	}

	/** Get all transitive predecessors of a node. */
	public Set<Node> getTransitivePredecessors(final Node node) {
		assert node != null;
		final Set<Node> predecessors = Sets.newHashSet();

		// We can do this because we know there are no cycles
		final List<Node> stack = Lists.newArrayList(getDirectPredecessors(node));
		while (!stack.isEmpty()) {
			final Node current = stack.remove(stack.size()-1);
			predecessors.add(current);
			stack.addAll(getDirectPredecessors(current));
		}

		return predecessors;
	}

	/** Get all nodes which are required to produce a given output. */
	public Set<Node> getTransitiveProducerSet(final String outputName) {
		assert outputName != null;
		if (!outputNameToNode.containsKey(outputName)) {
			throw new OutputNotFoundException("Could not find transitive predecssors for '" + outputName + "'.");
		}
		final Node node = outputNameToNode.get(outputName);
		final Set<Node> pred = getTransitivePredecessors(node);
		pred.add(node);
		return pred;
	}

	/** Get all direct successors of a node. */
	public Set<Node> getDirectSuccessors(final Node node) {
		assert node != null;
		if (!directSuccessors.containsKey(node)) {
			throw new NodeNotFoundException("Failed to get direct successors.  The requested node is not part of the graph.");
		}
		return Collections.unmodifiableSet(directSuccessors.get(node));
	}

	/** Creates a minimal graph with the requested outputs. */
	public Graph<Node> getSubGraphForOutputs(final Set<String> outputNames) {
		final Set<Node> subgraphNodes = Sets.newHashSet();
		for (final String outputName : outputNames) {
			subgraphNodes.addAll(getTransitiveProducerSet(outputName));
		}
		return new Graph<Node>(subgraphNodes);
	}

	private void init() {
		initInputOutputMaps();
		initGraph();
		checkCycles();
	}

	private void initInputOutputMaps() {
		for (final Node op : allNodes) {
			for (final String output : op.getOutputs()) {
				if (outputNameToNode.containsKey(output)) {
					throw new DuplicateOutputsFoundException("Duplicate output.");
				}
				outputNameToNode.put(output, op);
			}

			for (final String inputName : op.getInputs()) {
				if (!inputNameToNodes.containsKey(inputName)) {
					inputNameToNodes.put(inputName, Sets.<Node>newHashSet());
				}
				inputNameToNodes.get(inputName).add(op);
			}
		}
	}

	private void initGraph() {
		for (final Node op : allNodes) {
			directSuccessors.put(op, Sets.<Node>newHashSet());
			for (final String output : op.getOutputs()) {
				if (inputNameToNodes.containsKey(output)) {
					final Collection<Node> followers = inputNameToNodes.get(output);
					directSuccessors.get(op).addAll(followers);
				}
			}

			directPredecessors.put(op, Sets.<Node>newHashSet());
			for (final String input : op.getInputs()) {
				if (outputNameToNode.containsKey(input)) {
					directPredecessors.get(op).add(outputNameToNode.get(input));
				}
			}
		}
	}

	private void checkCycles() {
		GraphSort.sort(this);
	}

	private static final class GraphSort<T extends GraphNode> {

		public static <E extends GraphNode> List<E> sort(final Graph<E> system) {
			return new GraphSort<E>(system).sortInternal();
		}

		private final List<T> schedule = Lists.newArrayList();
		private final Map<T,Void> scheduling = Maps.newIdentityHashMap();
		private final Map<T,Void> scheduled = Maps.newIdentityHashMap();

		private final Graph<T> sys;

		private GraphSort(final Graph<T> sys) {
			this.sys = sys;
		}

		private List<T> sortInternal() {
			for (final T op : sys.getAllNodes()) {
				scheduleInternal(op);
			}
			return schedule;
		}

		private void scheduleInternal(final T op) {
			if (scheduled.containsKey(op)) {
				return;
			}

			if (scheduling.containsKey(op)) {
				throw new CyclicDependencyException();
			}

			try {
				scheduling.put(op, null);

				for (final T pred : sys.getDirectPredecessors(op)) {
					scheduleInternal(pred);
				}

				schedule.add(op);
				scheduled.put(op, null);
			} finally {
				scheduling.remove(op);
			}
		}
	}
}
