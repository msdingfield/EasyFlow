package msdingfield.easyflow.execution;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class DefaultExecutorTest {
	
	@Test
	public void test() throws InterruptedException, ExecutionException, TimeoutException {
		final ExecutorService es1 = DefaultExecutor.get();
		final ExecutorService es2 = DefaultExecutor.get();
		assertNotNull(es1);
		assertTrue(es1 == es2);
		final Future<Boolean> future;
		synchronized (this) {
			future = es1.submit(new Callable<Boolean>(){
				@Override public Boolean call() throws Exception {
					synchronized (DefaultExecutorTest.this) {
						DefaultExecutorTest.this.notifyAll();
					}
					return true;
				}});
		
			this.wait(1000L);
		}
		
		final Boolean result = future.get(1L, TimeUnit.SECONDS);
		assertTrue(result);
	}
}
