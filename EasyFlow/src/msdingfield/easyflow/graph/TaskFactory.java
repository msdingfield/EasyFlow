package msdingfield.easyflow.graph;

import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;

public interface TaskFactory<T extends FlowNode> {
	Task create(final Executor executor, final T node);
}
