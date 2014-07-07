package msdingfield.easyflow;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Represents a running or completed FlowGraph evaluation.
 * 
 * An evaluation can result in multiple exceptions.  None of the methods on the
 * methods here will throw exceptions due to errors in the evaluation of the
 * graph.  After completion, callers should use isSuccessful() and getErrors()
 * to check for evaluation errors.
 * 
 * Even if the evaluation fails, calling getOutputs() is still valid as the
 * evaluation may have partially complete results.
 * 
 * @author Matt
 *
 */
public interface FlowEvaluation {

	/**
	 * Block the current thread until the evaluation completes.
	 * @return this instance for chaining.
	 * @throws InterruptedException if the calling thread is interrupted.
	 */
	FlowEvaluation join() throws InterruptedException;

	/**
	 * Block the current thread until the evaluation completes or timeout is
	 * exceeded.
	 * 
	 * @param timeoutMs Timeout in milliseconds.
	 * @return this instance for chaining.
	 * @throws InterruptedException if the calling thread is interrupted.
	 */
	FlowEvaluation join(long timeoutMs) throws InterruptedException;

	/**
	 * Get the outputs from the evaluation.
	 * 
	 * This method will block if the evaluation is not complete.
	 * 
	 * @return Output values.
	 * @throws InterruptedException if the calling thread is interrupted.
	 */
	Map<String, Object> getOutputs() throws InterruptedException;

	/**
	 * Get the outputs from the evaluation.
	 * 
	 * This method will block if the evaluation is not complete.
	 * 
	 * @param timeoutMs Maximum time to block waiting for completion.
	 * @return Output values.
	 * @throws InterruptedException if the calling thread is interrupted.
	 * @throws TimeoutException If evaluation does not complete within the timeout.
	 */
	Map<String, Object> getOutputs(long timeoutMs) throws InterruptedException, TimeoutException;

	/**
	 * Add a callback to invoke when the evaluation completes.
	 * 
	 * The callback will be invoked in an evaluation thread.  Clients should
	 * avoid executing long running routines.
	 * 
	 * @param command The callback to invoke.
	 * @return this instance for chaining.
	 */
	FlowEvaluation addCallback(final Runnable command);

	/**
	 * Determine if the evaluation is complete.
	 * 
	 * @return True if the evaluation is complete.
	 */
	boolean isDone();

	/**
	 * Determine if the evaluation is complete and has no errors.
	 * @return True if successful.
	 */
	boolean isSuccessful();

	/**
	 * Get any errors produced during evaluation.
	 * 
	 * This will be an empty collection if no errors have occurred.
	 * 
	 * @return Errors produced during evaluation.
	 */
	Collection<Throwable> getErrors();
}
