package msdingfield.easyflow.graph;

import java.util.Set;

/** A node in a directed graph.
 * 
 * Rather than representing a graph with adjacency lists or matrices, a graph
 * is represented with named edges.  Each node holds a set of names of incoming
 * edges and a set of names of outgoing edges.
 * 
 * @author Matt
 *
 */
public interface FlowNode {

	Set<String> getOutputs();
	
	Set<String> getInputs();
}
