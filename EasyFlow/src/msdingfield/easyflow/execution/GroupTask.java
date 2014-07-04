package msdingfield.easyflow.execution;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;

/**
 * A Task which groups a collection of tasks together.
 * 
 * The GroupTasks only completes when all of the subtasks complete.  All of the
 * subtasks are scheduled when the GroupTask is scheduled.
 * 
 * It is not enforced that the subtasks share the same Executor.
 * 
 * @author Matt
 *
 */
public class GroupTask extends Task {

	/** The collection of subtasks in the group. */
	private final Collection<Task> subtasks = Lists.newArrayList();

	/** Create a GroupTask with the executor and subtasks. */
	public GroupTask(final Executor executor, final Collection<? extends Task> subtasks) {
		super(executor);
		this.subtasks.addAll(subtasks);
		this.waitFor(subtasks);
	}

	/** Schedule subtasks and this GroupTasks. */
	@Override
	public Task schedule() {
		for (final Task task : subtasks) {
			task.schedule();
		}

		super.schedule();

		return this;
	}

	@Override
	public String toString() {
		return "GroupTask [super=" + super.toString() +", subtasks=" + subtasks + "]";
	}

}
