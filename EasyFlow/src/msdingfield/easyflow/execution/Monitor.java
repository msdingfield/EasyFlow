package msdingfield.easyflow.execution;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

/**
 * Signal listeners when blockers reaches 0.
 * 
 * Not to be confused with the monitor attached to each Java object, a Monitor
 * acts similar to a write lock.  A lock is obtained by calling acquire() and
 * returned by calling release().  However, unlike a conventional write lock
 * which blocks writers, a Monitor calls a collection of listeners whenever it
 * becomes unblocked.
 * 
 * In addition to calling listeners, waiters are notified via the
 * Object.notifyAll() method.  This allows threads to block on Object.wait()
 * until the number of blockers reaches 0.
 * 
 * Note that it is the transition to 0 blockers that triggers notification,
 * thus no listeners will be called if there are never any blockers.
 * 
 * @author Matt
 *
 */
public class Monitor {

	private final AtomicInteger lockCount = new AtomicInteger(0);
	private final Collection<Runnable> listeners = Lists.newArrayList();

	/** Acquire a lock. */
	public void acquire() {
		if (lockCount.incrementAndGet() < 0) {
			throw new OverflowException();
		}
	}

	/** Release a lock. */
	public synchronized void release() {
		final int count = lockCount.decrementAndGet();
		if (count == 0) {
			for (final Runnable listener : listeners) {
				listener.run();
			}

			notifyAll();

		} else if (count < 0) {
			throw new UnderflowException();
		}
	}

	/** True if there are more than 0 blockers. */
	public boolean isLocked() {
		return lockCount.get() != 0;
	}

	/** Add a listener to be called when blockers goes to zero. */
	public synchronized void addListener(final Runnable observer) {
		listeners.add(observer);
	}

	/** Exception thrown integer overflow occurs in the lock count. */
	public class OverflowException extends RuntimeException {
		private static final long serialVersionUID = -2951628390702086008L;
		public OverflowException() {
			super("Maximum number of lock permits exceeded.");
		}
	}

	/** Exception thrown if release() is called and there are no blockers. */
	public class UnderflowException extends RuntimeException {
		private static final long serialVersionUID = -2951628390702086008L;
		public UnderflowException() {
			super("Permits released exceeds permits acquired.");
		}
	}

	@Override
	public String toString() {
		return "Monitor [lockCount=" + lockCount + "]";
	}

}
