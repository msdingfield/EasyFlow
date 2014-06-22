package msdingfield.easyflow.reflect;

import java.util.List;
import java.util.Set;

import msdingfield.easyflow.graph.FlowNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
		for (final OperationInputPort getter : op.getOutputs()) {
			outputs.add(getter.getName());
		}
		return outputs;
	}

	@Override
	public Set<String> getInputs() {
		final Set<String> inputPorts = Sets.newHashSet();
		for (final OperationOutputPort setter : op.getInputs()) {
			inputPorts.add(setter.getName());
		}
		return inputPorts;
	}

	public ClassOperation getOp() {
		return op;
	}
}
