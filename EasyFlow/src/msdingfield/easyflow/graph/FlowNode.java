package msdingfield.easyflow.graph;

import java.util.Set;

/** An operation to be performed.
 * 
 * Operations have a set of inputs and a set of outputs.  All of the inputs 
 * must be available before the operation can execute.  Upon completion, all
 * outputs must have a value.
 * 
 * The inputs must be of a compatible type or evaluation will fail.
 * 
 * Not that this class is immutable.
 * 
 * @author Matt
 *
 */
public interface FlowNode {

	Set<String> getOutputs();
	
	Set<String> getInputs();
}
