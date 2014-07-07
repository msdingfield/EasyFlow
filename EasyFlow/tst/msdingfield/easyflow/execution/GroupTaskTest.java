package msdingfield.easyflow.execution;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.collect.Lists;

public class GroupTaskTest {

	@Test
	public void testEmptyGroup() throws InterruptedException {
		final Executor executor = DefaultExecutor.get();
		
		final GroupTask gt = new GroupTask(executor, Collections.<Task>emptyList());
		
		gt.schedule().join();
	}
	
	@Test
	public void testSingleTask() throws InterruptedException {
		final AtomicInteger state = new AtomicInteger(0);
		final Executor executor = DefaultExecutor.get();
		final Task task = new Task(executor, new Runnable(){
			@Override public void run() {
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e) {
				}
				state.compareAndSet(0, 1);
			}});
		
		final GroupTask gt = new GroupTask(executor, Lists.newArrayList(task));
		gt.schedule().join();
		assertEquals(1, state.get());
	}
}
