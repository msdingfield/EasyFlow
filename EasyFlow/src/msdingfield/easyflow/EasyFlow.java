package msdingfield.easyflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.graph.Graph;
import msdingfield.easyflow.graph.GraphTaskBuilder;
import msdingfield.easyflow.reflect.ClassOperation;
import msdingfield.easyflow.reflect.ClassOperationFlowNode;
import msdingfield.easyflow.reflect.ClassOperationTaskFactory;
import msdingfield.easyflow.reflect.ClassPathScannerClassOperationBuilder;
import msdingfield.easyflow.reflect.Context;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * APIs for running a flow graph.
 * 
 * @author Matt
 *
 */
public final class EasyFlow {
	private EasyFlow() {}

	/**
	 * Load a FlowGraph.
	 * 
	 * This scans the classpath for graph with the given name.
	 * 
	 * Loading a graph is an expensive operation and should only be done once.
	 * The returned graph instance is immutable and can be evaluated multiple
	 * times sequentially or concurrently.
	 * 
	 * @param basePkg Search only this java package and sub-packages.
	 * @param graphName Name of the graph.
	 * @return
	 */
	public static FlowGraph loadFlowGraph(
			final String basePkg, final String graphName) {
		final List<ClassOperation> operations = ClassPathScannerClassOperationBuilder.loadOperationsOnClasspath(basePkg, graphName);
		final Graph<ClassOperationFlowNode> graph = new Graph<ClassOperationFlowNode>(Sets.newHashSet(Lists.transform(operations, new Function<ClassOperation, ClassOperationFlowNode>(){
			@Override public ClassOperationFlowNode apply(final ClassOperation op) {
				return new ClassOperationFlowNode(op);
			}})));
		return new FlowGraphImpl(graph);
	}

	private static class FlowGraphImpl implements FlowGraph {
		private final Graph<ClassOperationFlowNode> impl;

		public FlowGraphImpl(final Graph<ClassOperationFlowNode> impl) {
			this.impl = impl;
		}

		@Override
		public FlowEvaluation evaluate(final Map<String, Object> params) {
			final Context context = new Context(params);
			final Task task = GraphTaskBuilder
					.graph(impl)
					.taskFactory(new ClassOperationTaskFactory(context))
					.build()
					.schedule();
			return new FlowTaskImpl(task, context);
		}

	}

	private static class FlowTaskImpl implements FlowEvaluation {
		private final Task task;
		private final Context context;

		public FlowTaskImpl(final Task task, final Context context) {
			this.task = task;
			this.context = context;
		}

		@Override
		public FlowEvaluation join() throws InterruptedException {
			task.join();
			return this;
		}

		@Override
		public FlowEvaluation join(final long timeoutMs) throws InterruptedException {
			task.join(timeoutMs);
			return this;
		}

		@Override
		public FlowEvaluation addCallback(final Runnable command) {
			task.addCompletionListener(command);
			return this;
		}

		@Override
		public boolean isDone() {
			return task.isComplete();
		}

		@Override
		public boolean isSuccessful() {
			return task.isSuccess();
		}

		@Override
		public Collection<Throwable> getErrors() {
			return task.getErrors();
		}

		@Override
		public Map<String, Object> getOutputs() throws InterruptedException {
			join();
			return getOutputNoWait();
		}

		@Override
		public Map<String, Object> getOutputs(final long timeoutMs)
				throws InterruptedException, TimeoutException {
			join(timeoutMs);
			if (!isDone()) {
				throw new TimeoutException();
			}
			return getOutputNoWait();
		}

		private Map<String, Object> getOutputNoWait() {
			final Set<String> keys = context.getEdgeKeys();
			final Map<String, Object> outputs = Maps.newHashMap();
			for (final String key : keys) {
				outputs.put(key, context.getEdgeValue(key));
			}
			return outputs;
		}

	}
}
