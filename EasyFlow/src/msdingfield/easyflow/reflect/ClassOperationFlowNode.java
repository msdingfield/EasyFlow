package msdingfield.easyflow.reflect;

import java.util.List;
import java.util.Set;

import msdingfield.easyflow.graph.FlowNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * FlowNode implemented in terms of ClassOperation.
 * 
 * This class adapts a ClassOperation so that dependencies among 
 * ClassOperations can be analyzed using the FlowGraph class.
 * 
 * @author Matt
 *
 */
public class ClassOperationFlowNode implements FlowNode {
	private final ClassOperation op;
	
	public static ClassOperationFlowNode toFlowNode(final ClassOperation op) {
		return new ClassOperationFlowNode(op);
	}
	
	public static List<ClassOperationFlowNode> toFlowNodes(final List<ClassOperation> ops) {
		return Lists.transform(ops, new Function<ClassOperation, ClassOperationFlowNode>(){
			@Override public ClassOperationFlowNode apply(ClassOperation arg0) {
				return toFlowNode(arg0);
			}});
	}
	
	public ClassOperationFlowNode(final ClassOperation op) {
		this.op = op;
	}
	
	@Override
	public Set<String> getOutputs() {
		final Set<String> outputs = Sets.newHashSet();
		for (final OperationOutputPort getter : op.getOutputs()) {
			outputs.add(getter.getName());
		}
		return outputs;
	}

	@Override
	public Set<String> getInputs() {
		final Set<String> inputPorts = Sets.newHashSet();
		for (final OperationInputPort setter : op.getInputs()) {
			inputPorts.add(setter.getName());
		}
		return inputPorts;
	}

	public ClassOperation getOp() {
		return op;
	}

	@Override
	public String toString() {
		return "ClassOperationFlowNode [op=" + op + ", getOutputs()="
				+ getOutputs() + ", getInputs()=" + getInputs() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassOperationFlowNode other = (ClassOperationFlowNode) obj;
		if (op == null) {
			if (other.op != null)
				return false;
		} else if (!op.equals(other.op))
			return false;
		return true;
	}
}
