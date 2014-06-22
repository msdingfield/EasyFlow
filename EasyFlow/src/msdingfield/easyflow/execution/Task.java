package msdingfield.easyflow.execution;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;

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
	
	private static final ThreadLocal<Task> currentTask = new ThreadLocal<>();
	
	/** Monitors scheduled work items. */
	private final Monitor scheduledWork = new Monitor();
	
	enum State { UNSCHEDULED, BLOCKED, INITIALIZING, EXECUTING, FINALIZING, COMPLETE }
	private State state = State.UNSCHEDULED;
	
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
	
	public synchronized void waitForCompletion() throws InterruptedException {
		if (isScheduled() && !isComplete()) {
			this.wait();
		}
	}
	
	private boolean isComplete() {
		return state == State.COMPLETE;
	}

	public synchronized void waitFor(final Task ... predecessors) {
		checkUnsheduled();
		
		for (final Task predecessor : predecessors) {
			scheduledWork.acquire();
			
			predecessor.addCompletionListener(new Runnable() {
				@Override
				public void run() {
					scheduledWork.release();
				}
			});
		}
	}

	public synchronized void waitFor(final Collection<? extends Task> predecessors) {
		checkUnsheduled();
		
		for (final Task predecessor : predecessors) {
			scheduledWork.acquire();
			
			predecessor.addCompletionListener(new Runnable() {
				@Override
				public void run() {
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
			} finally {
				currentTask.set(previousTask);
				scheduledWork.release();
			}
		}
	}

	public static class ForkFromNonTaskThreadException extends RuntimeException {
		private static final long serialVersionUID = -228232928568742825L;
		public ForkFromNonTaskThreadException() {
			super("An attempt was made to fork from a thread not bound to a task.");
		}
	}

}
