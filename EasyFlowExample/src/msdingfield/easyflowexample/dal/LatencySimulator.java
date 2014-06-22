package msdingfield.easyflowexample.dal;

import java.util.concurrent.PriorityBlockingQueue;

import com.google.common.util.concurrent.SettableFuture;

public final class LatencySimulator {
	
	private static final PriorityBlockingQueue<Setter<?>> queue = new PriorityBlockingQueue<>();
	private static final Thread thread = new Thread() {

		@Override
		public void run() {
			try {
				for(;;) {
					final Setter<?> setter = queue.take();
					final long now = System.currentTimeMillis();
					final long msToWait = setter.time - now;
					if (msToWait > 0) {
						sleep(msToWait);
					}
					setter.doSet();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	};

	public static <T> void setWithLatency(final SettableFuture<T> future, final T value, final long millis) {
		if (!thread.isAlive()) {
			synchronized (thread) {
				if (!thread.isAlive()) {
					thread.setDaemon(true);
					thread.start();
				}
			}
		}
		queue.add(new Setter<T>(future, value, System.currentTimeMillis() + millis));
	}
	
	private static class Setter<T> implements Comparable<Setter<T>>{

		private final SettableFuture<T> future;
		private final T value;
		private final long time;
		
		public Setter(final SettableFuture<T> future, final T value, final long time) {
			this.future = future;
			this.value = value;
			this.time = time;
		}
		
		public void doSet() {
			future.set(value);
		}
		
		@Override
		public int compareTo(final Setter<T> other) {
			return Long.compare(this.time, other.time);
		}
		
	}
	
	private LatencySimulator() {}
}
