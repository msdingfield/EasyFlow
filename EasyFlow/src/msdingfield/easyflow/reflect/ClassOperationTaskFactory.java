package msdingfield.easyflow.reflect;

import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.graph.TaskFactory;

/** Create Task instances for ClassOperationFlowNode instances.
 * 
 * Construct the factory with a Context instance.  The Task instances created
 * are bound to the Context instance, Executor and ClassOperation contained 
 * within the ClassOperationFlowNode.
 * 
 * @author Matt
 *
 */
public class ClassOperationTaskFactory implements TaskFactory<ClassOperationFlowNode> {

	private final Context context;
	
	public ClassOperationTaskFactory(final Context context) {
		this.context = context;
	}
	
	/**
	 * Create a new Task bound to context, executor and node.getOp().
	 */
	@Override
	public Task create(final Executor executor, final ClassOperationFlowNode node) {
		return create(executor, node.getOp(), context);
	}
	
	/**
	 * Static helper creating a Task bound to executor, op and context.
	 * 
	 * @param executor Executor instance in which task will run.
	 * @param op The ClassOperation the Task will delegate to.
	 * @param context The Context instance which will hold intermediate state.
	 * @return The newly created Task.
	 */
	public static Task create(final Executor executor, final ClassOperationProxy op, final Context context) {
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
}
