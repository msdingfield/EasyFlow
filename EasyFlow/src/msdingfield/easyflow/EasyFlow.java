package msdingfield.easyflow;

import java.util.List;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.graph.FlowGraph;
import msdingfield.easyflow.graph.FlowGraphTaskBuilder;
import msdingfield.easyflow.reflect.ClassOperation;
import msdingfield.easyflow.reflect.ClassOperationFlowNode;
import msdingfield.easyflow.reflect.ClassOperationTaskFactory;
import msdingfield.easyflow.reflect.ClassPathScannerClassOperationBuilder;
import msdingfield.easyflow.reflect.Context;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class EasyFlow {

	public static FlowGraph<ClassOperationFlowNode> loadFlowGraph(
			final String basePkg, final String scope) {
		final List<ClassOperation> operations = ClassPathScannerClassOperationBuilder.loadOperationsOnClasspath(basePkg, scope);
		final FlowGraph<ClassOperationFlowNode> system = new FlowGraph<ClassOperationFlowNode>(Sets.newHashSet(Lists.transform(operations, new Function<ClassOperation, ClassOperationFlowNode>(){
			@Override public ClassOperationFlowNode apply(final ClassOperation arg0) {
				return new ClassOperationFlowNode(arg0);
			}})));
		return system;
	}

	public static Task evaluate(final FlowGraph<ClassOperationFlowNode> system, final Context context) {
		final Task task = FlowGraphTaskBuilder
				.graph(system)
				.taskFactory(new ClassOperationTaskFactory(context))
				.build()
				.schedule();
		return task;
	}
}
