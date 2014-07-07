package msdingfield.easyflow.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * A Task is a logical work item with dependencies.
 * 
 * The execution graph is a directed acyclic graph of TaskS.  The graph defines
 * the order in which the tasks may execute.
 * 
 * The task goes through the states defined by the State enum.  Runnables can
 * be registered to execute at various points in the lifecycle.
 * 
 * @author Matt
 *
 */
public class Task {

	private final Executor executor;
	private final Collection<Runnable> workers = Lists.newArrayList();
	private final Collection<Runnable> initializers = Lists.newArrayList();
	private final Collection<Runnable> finalizers = Lists.newArrayList();
	private final Collection<Runnable> completionListeners = Lists.newArrayList();
	private final List<Throwable> errors = new Vector<>();

	private static final ThreadLocal<Task> currentTask = new ThreadLocal<>();

	/** Monitors scheduled work items. */
	private final Monitor scheduledWork = new Monitor();

	enum State { UNSCHEDULED, BLOCKED, INITIALIZING, EXECUTING, FINALIZING, COMPLETE }
	private volatile State state = State.UNSCHEDULED;

	/** Create a task to invoke the given Runnable. */
	public Task(final Executor executor) {
		this.executor = executor;
		scheduledWork.addListener(new Runnable() {
			@Override public void run() {
				onQuiet();
			}});
	}

	public Task(final Executor executor, final Runnable worker) {
		this(executor);
		addWorker(worker);
	}

	public synchronized Task addWorker(final Runnable worker) {
		checkUnsheduled();
		workers.add(worker);
		return this;
	}

	public synchronized void addInitializer(final Runnable initializer) {
		checkUnsheduled();
		initializers.add(initializer);
	}

	public synchronized void addFinalizer(final Runnable finalizer) {
		checkUnsheduled();
		finalizers.add(finalizer);
	}

	public synchronized void addCompletionListener(final Runnable listener) {
		checkUnsheduled();
		completionListeners.add(listener);
	}

	public synchronized void join() throws InterruptedException {
		if (isScheduled() && !isComplete()) {
			this.wait();
		}
	}

	public synchronized void join(final long timeout) throws InterruptedException {
		if (isScheduled() && !isComplete()) {
			this.wait(timeout);
		}
	}

	public synchronized void join(final long timeout, final int nanos) throws InterruptedException {
		if (isScheduled() && !isComplete()) {
			this.wait(timeout, nanos);
		}
	}

	public boolean isComplete() {
		return state == State.COMPLETE;
	}

	public boolean isSuccess() {
		return isComplete() && errors.isEmpty();
	}

	public Collection<Throwable> getErrors() {
		return Lists.newArrayList(errors);
	}

	public void waitFor(final Task ... predecessors) {
		waitFor(Lists.newArrayList(predecessors));
	}

	public synchronized void waitFor(final Collection<? extends Task> predecessors) {
		checkUnsheduled();

		for (final Task predecessor : predecessors) {
			scheduledWork.acquire();

			predecessor.addCompletionListener(new Runnable() {
				@Override
				public void run() {
					if (predecessor.isInError()) {
						errors.add(new DependencyFailureException(predecessor));
					}
					scheduledWork.release();
				}
			});
		}
	}

	public synchronized Task schedule() {
		checkUnsheduled();
		setState(State.BLOCKED);
		return this;
	}

	private void checkUnsheduled() {
		if (isScheduled()) {
			throw new IllegalStateException();
		}
	}

	public boolean isScheduled() {
		return state != State.UNSCHEDULED;
	}

	/** Execute a Runnable in the context of this task.
	 * 
	 * @param runnable The runnable instance to execute.
	 */
	private void execute(final Runnable runnable) {
		scheduledWork.acquire();
		executor.execute(new Worker(runnable));
	}

	private void executeWhenDone(final ListenableFuture<?> future, final Runnable runnable) {
		scheduledWork.acquire();
		future.addListener(new Worker(runnable), executor);
	}

	/** Forks the current task.
	 * 
	 * The current task bound to the thread will not complete until the passed
	 * runnable completes.
	 * 
	 * @param runnable
	 */
	public static void fork(final Runnable runnable) {
		final Task task = currentTask.get();
		if (task == null) {
			throw new ForkFromNonTaskThreadException();
		}
		task.execute(runnable);
	}

	public static void fork(final ListenableFuture<?> future, final Runnable runnable) {
		final Task task = currentTask.get();
		if (task == null) {
			throw new ForkFromNonTaskThreadException();
		}
		task.executeWhenDone(future, runnable);
	}

	public static <T> ListenableFuture<List<T>> combineFutures(final List<ListenableFuture<T>> futures) {
		final AbstractFuture<List<T>> future = new FutureCombiner<T>(futures);
		return future;
	}

	private void onQuiet() {
		switch(state) {
		case UNSCHEDULED: break;
		case COMPLETE: break;
		case BLOCKED:
			setState(State.INITIALIZING);
			break;
		case INITIALIZING:
			setState(State.EXECUTING);
			break;
		case EXECUTING:
			setState(State.FINALIZING);
			break;
		case FINALIZING:
			setState(State.COMPLETE);
			break;
		}
	}

	private void setState(final State state) {
		if (state != State.COMPLETE && isInError()) {
			setState(State.COMPLETE);
			return;
		}

		this.state = state;
		switch(state) {
		case UNSCHEDULED: break;
		case BLOCKED:
			if (!scheduledWork.isLocked()) {
				setState(State.INITIALIZING);
			}
			break;
		case INITIALIZING:
			startInitializing();
			break;
		case EXECUTING:
			startExecuting();
			break;
		case FINALIZING:
			startFinalizing();
			break;
		case COMPLETE:
			notifyTaskWaiters();
			break;
		}
	}

	private boolean isInError() {
		return !errors.isEmpty();
	}

	private void startInitializing() {
		forkRunnables(initializers);
	}

	private void startExecuting() {
		forkRunnables(workers);
	}

	private void startFinalizing() {
		forkRunnables(finalizers);
	}

	private synchronized void notifyTaskWaiters() {
		for (final Runnable runnable : completionListeners) {
			runnable.run();
		}
		notifyAll();
	}

	private void forkRunnables(final Collection<Runnable> runnables) {
		try {
			scheduledWork.acquire();
			for (final Runnable runnable : runnables) {
				execute(runnable);
			}
		} finally {
			scheduledWork.release();
		}
	}

	public static void addFatalError(final Throwable e) {
		final Task task = currentTask.get();
		task.errors.add(e);
	}

	public static void addFatalError(final String message, final Exception cause) {
		addFatalError(new FatalErrorException(message, cause));
	}
	private class Worker implements Runnable {
		private final Runnable inner;
		public Worker(final Runnable inner) {
			this.inner = inner;
		}

		@Override
		public void run() {
			// If executor runs in same thread we can get recursive calls here
			// we need to make sure we restore the correct value.
			final Task previousTask = currentTask.get();
			try {
				currentTask.set(Task.this);
				inner.run();
			} catch (final Throwable t) {
				addFatalError(t);
			} finally {
				currentTask.set(previousTask);
				scheduledWork.release();
			}
		}
	}

	private static final class FutureCombiner<T> extends
	AbstractFuture<List<T>> {

		private final AtomicInteger remaining;
		private final ArrayList<T> values;

		public FutureCombiner(final List<ListenableFuture<T>> futures) {
			remaining = new AtomicInteger(futures.size());
			values = new ArrayList<T>();
			for (int n = 0; n < futures.size(); ++ n) {
				values.add(null);
			}
			int i = 0;
			for (final ListenableFuture<T> item : futures) {
				final int index = i++;
				setItem(item, index);
			}
		}

		private void setItem(final ListenableFuture<T> item, final int index) {
			fork(item, new Runnable(){
				@Override public void run() {
					try {
						values.set(index, Uninterruptibles.getUninterruptibly(item));
						if (remaining.decrementAndGet() == 0) {
							set(values);
						}
					} catch (final ExecutionException e) {
						setException(e);
						throw new FatalErrorException("Error unwinding list of futures.", e);
					}
				}});
		}
	}

	public static class ForkFromNonTaskThreadException extends RuntimeException {
		private static final long serialVersionUID = -228232928568742825L;
		public ForkFromNonTaskThreadException() {
			super("An attempt was made to fork from a thread not bound to a task.");
		}
	}

	public static class FatalErrorException extends RuntimeException {
		public FatalErrorException() {
			super();
		}

		public FatalErrorException(final String message, final Throwable cause) {
			super(message, cause);
		}

		public FatalErrorException(final String message) {
			super(message);
		}

		public FatalErrorException(final Throwable cause) {
			super(cause);
		}

		private static final long serialVersionUID = 6797297276936778661L;

	}

	public static final class DependencyFailureException extends RuntimeException {
		private static final long serialVersionUID = -5150369448056789457L;
		private final Task predecessor;

		public DependencyFailureException(final Task predecessor) {
			super("A task required by this task failed.");
			this.predecessor = predecessor;
		}

		public Task getPredecessor() {
			return predecessor;
		}
	}

	@Override
	public String toString() {
		return "Task [workers=" + workers + ", errors=" + errors
				+ ", scheduledWork=" + scheduledWork + ", state=" + state + "]\n";
	}

}
