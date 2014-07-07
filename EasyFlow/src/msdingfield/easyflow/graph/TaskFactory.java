package msdingfield.easyflow.graph;

import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;

/** Factory for creating Task instances for FlowNode instances. */
public interface TaskFactory<T extends GraphNode> {
	
	/**
	 * Create a Task instance which corresponds to the given FlowNode.  The 
	 * task is bound to the provided executor.
	 * 
	 * @param executor Executor used to invoke task.
	 * @param node Node describing the task to be performed.
	 * @return The created Task.
	 */
	Task create(final Executor executor, final T node);
}
