package msdingfield.easyflow.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Holder for a default Executor.
 * 
 * This holds a single ExecutorService with a fixed number of threads equal to
 * the number of available processors.  Operations are not expected to block 
 * so we don't need more threads than we have processors.
 * 
 * @author Matt
 *
 */
public final class DefaultExecutor {

	/** The ExecutorService instance. */
	private static volatile ExecutorService executor = null;

	/** Get the ExecutorService instance.  Create if needed. */
	public static ExecutorService get() {
		if (executor == null) {
			synchronized (DefaultExecutor.class) {
				if (executor == null) {
					executor = Executors.newFixedThreadPool(
							Runtime.getRuntime().availableProcessors(), 
							new ThreadFactoryBuilder().setDaemon(true).setNameFormat("flow-pool-%d").build());
				}
			}
		}
		return executor;
	}

}
