package msdingfield.easyflowexample.dal;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import msdingfield.easyflowexample.dal.LatencySimulator;

import org.junit.Test;

import com.google.common.util.concurrent.SettableFuture;

public class LatencySimulatorTest {
	
	private static final Executor exec = Executors.newSingleThreadExecutor();

	private long f1Time = 0;
	private long f2Time = 0;
	
	@Test
	public void testLatency() throws InterruptedException, ExecutionException {
		final SettableFuture<String> f1 = SettableFuture.create();
		f1.addListener(new Runnable(){

			@Override
			public void run() {
				f1Time = System.currentTimeMillis();
				synchronized (LatencySimulatorTest.this) {
					LatencySimulatorTest.this.notify();
				}
			}}, exec);
		
		final SettableFuture<String> f2 = SettableFuture.create();
		f2.addListener(new Runnable(){

			@Override
			public void run() {
				f2Time = System.currentTimeMillis();
				synchronized (LatencySimulatorTest.this) {
					LatencySimulatorTest.this.notify();
				}
			}}, exec);
		
		final long start = System.currentTimeMillis();
		LatencySimulator.setWithLatency(f2, "f2", 1000L);
		LatencySimulator.setWithLatency(f1, "f1", 200L);
		
		assertEquals("f1", f1.get());
		assertEquals("f2", f2.get());
		
		// Make sure f1 & f2 callbacks have finished
		synchronized (this) {
			this.wait();
		}
		
		final long f1Pause = f1Time - start;
		assertTrue(String.format("%d", f1Pause), f1Pause < 250L && f1Pause >= 200L);
		
		final long f2Pause = f2Time - start;
		assertTrue(String.format("%d", f2Pause), f2Pause < 1050L && f2Pause >= 1000L);
		
	}
}
