package msdingfield.easyflow.graph;

import java.util.Map;
import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.DefaultExecutor;
import msdingfield.easyflow.execution.GroupTask;
import msdingfield.easyflow.execution.Task;

import com.google.common.collect.Maps;

/**
 * A builder for creating a Task which executes a FlowGraph.
 * 
 * FlowGraphTaskBuilder
 *  .graph(graph)
 *  .taskFactory(taskFactory)
 *  [.executor(executor)]
 *  .build()
 *  
 * @author Matt
 *
 */
public class FlowGraphTaskBuilder {

	public static <T extends FlowNode> FactoryStep<T> graph(final FlowGraph<T> graph) {
		return new Builder<T>().graph(graph);
	}
	
	public static interface FactoryStep<T extends FlowNode> {
		ExecutorStep<T> taskFactory(final TaskFactory<T> factory);
	}

	public static interface ExecutorStep<T extends FlowNode> extends BuildStep<T> {
		BuildStep<T> executor(final Executor executor);
	}
	
	public static interface BuildStep<T extends FlowNode> {
		Task build();
	}

	public static class Builder<T extends FlowNode> implements FactoryStep<T>, ExecutorStep<T>, BuildStep<T> {
		private FlowGraph<T> graph = null;
		private TaskFactory<T> factory = null;
		private Executor executor = DefaultExecutor.get();
		
		public FactoryStep<T> graph(final FlowGraph<T> graph) {
			this.graph = graph;
			return this;
		}

		@Override
		public ExecutorStep<T> taskFactory(final TaskFactory<T> factory) {
			this.factory = factory;
			return this;
		}

		@Override
		public BuildStep<T> executor(final Executor executor) {
			this.executor = executor;
			return this;
		}
		
		@Override
		public Task build() {
			final Map<T, Task> table = Maps.newHashMap();
			
			// Initialize runners
			for (final T op : graph.getAllNodes()) {
				final Task task = factory.create(executor, op);
				table.put(op, task);
			}
			
			// Setup graph
			for (final Map.Entry<T, Task> entry : table.entrySet()) {
				final T currentOp = entry.getKey();
				final Task currentTask = entry.getValue();
				for (final T predecessorOp : graph.getDirectPredecessors(currentOp)) {
					final Task predecessorTask = table.get(predecessorOp);
					currentTask.waitFor(predecessorTask);
				}
			}

			return new GroupTask(executor, table.values());
		}

	}
}
