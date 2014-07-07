package msdingfield.easyflow.testsupport;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class TimerFuture<T> implements ListenableFuture<T> {
	private static final Timer timer = new Timer(true);
	private final SettableFuture<T> settable = SettableFuture.create();
	
	public TimerFuture(final long timeMs, final T value) {
		timer.schedule(new TimerTask() {
			@Override public void run() {
				settable.set(value);
			}}, timeMs);
	}

	public void addListener(Runnable listener, Executor exec) {
		settable.addListener(listener, exec);
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return settable.cancel(mayInterruptIfRunning);
	}

	public T get() throws InterruptedException, ExecutionException {
		return settable.get();
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException, ExecutionException {
		return settable.get(timeout, unit);
	}

	public boolean isCancelled() {
		return settable.isCancelled();
	}

	public boolean isDone() {
		return settable.isDone();
	}

	public boolean setException(Throwable throwable) {
		return settable.setException(throwable);
	}
}
