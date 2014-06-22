package msdingfield.easyflow.reflect;

import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.graph.TaskFactory;

public class ClassOperationTaskFactory implements TaskFactory<ClassOperationFlowNode>{

	private final Context context;
	public ClassOperationTaskFactory(final Context context) {
		this.context = context;
	}
	
	@Override
	public Task create(final Executor executor, final ClassOperationFlowNode node) {
		return create(executor, node.getOp(), context);
	}
	
	public static Task create(final Executor executor, final ClassOperation op, final Context context) {
		final Task task = new Task(executor);
		task.addWorker(new Runnable(){
			@Override public void run() {
				op.execute(context);
			}});
		task.addInitializer(new Runnable(){
			@Override public void run() {
				op.before(context);
			}});
		task.addFinalizer(new Runnable(){
			@Override public void run() {
				op.after(context);
			}});
		return task;
	}

	public static ClassOperationTaskFactory get(Context context2) {
		return new ClassOperationTaskFactory(context2);
	}
}
