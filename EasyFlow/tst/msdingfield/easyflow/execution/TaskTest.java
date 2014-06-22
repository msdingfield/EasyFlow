package msdingfield.easyflow.execution;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import msdingfield.easyflow.execution.Task;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class TaskTest {
	
	@Rule 
	public JUnitRuleMockery context = new JUnitRuleMockery();

	@Rule
	public ExpectedException exception = ExpectedException.none();
		
	@Mock
	private Runnable runnableA;
	
	@Mock
	private Runnable runnableB;
	
	private Executor executor = new Executor(){

		@Override
		public void execute(final Runnable command) {
			command.run();
		}};

	private Task task;

	@Before
	public void setup() {
		task = new Task(executor, runnableA);
	}

	@Test
	public void testNewTaskInvokable() {
		context.checking(new Expectations(){{
			oneOf(runnableA).run();
		}});
		
		task.schedule();
	}
	
	@Test
	public void testInvokeChild() throws InterruptedException {
		context.checking(new Expectations(){{
			oneOf(runnableA).run();
			oneOf(runnableB).run();
		}});
		task.addWorker(new Runnable(){

			@Override
			public void run() {
				Task.fork(runnableB);
			}});
		task.schedule();
		task.waitForCompletion();
	}
	
	@Test
	public void testFork() {
		context.checking(new Expectations(){{
			oneOf(runnableA).run();
			oneOf(runnableB).run();
		}});
		
		task = new Task(executor, new Runnable(){

			@Override
			public void run() {
				Task.fork(runnableA);
				Task.fork(runnableB);
			}});
		
		task.schedule();
	}
	
	@Test
	public void testForkFromNonTask() {
		exception.expect(Task.ForkFromNonTaskThreadException.class);
		Task.fork(runnableA);
	}
	
	@Test
	public void testSinglePredecessor() throws InterruptedException {
		context.checking(new Expectations(){{
			oneOf(runnableA).run();
			oneOf(runnableB).run();
		}});
		
		final Task predecessor = new Task(executor, runnableB);
		task.waitFor(predecessor);
		predecessor.schedule();
		task.schedule();
		task.waitForCompletion();
	}
	
	@Test
	public void testInvokeOneOfTwoPredecessors() {
		context.checking(new Expectations(){{
			never(runnableA).run();
			oneOf(runnableB).run();
		}});
		
		final Task predecessor1 = new Task(executor, runnableB);
		task.waitFor(predecessor1);
		
		final Task predecessor2 = new Task(executor, runnableB);
		task.waitFor(predecessor2);
		
		predecessor1.schedule();
	}

	@Test
	public void testInvokeTwoOfTwoPredecessors() throws InterruptedException {
		context.checking(new Expectations(){{
			oneOf(runnableA).run();
			exactly(2).of(runnableB).run();
		}});
		
		final Task predecessor1 = new Task(executor, runnableB);
		task.waitFor(predecessor1);
		
		final Task predecessor2 = new Task(executor, runnableB);
		task.waitFor(predecessor2);
		
		task.schedule();
		predecessor1.schedule();
		predecessor2.schedule();
		task.waitForCompletion();
	}
	
	@Test
	public void testFanOut() throws InterruptedException {

		context.checking(new Expectations(){{
			oneOf(runnableA).run();
			exactly(2).of(runnableB).run();
		}});
		
		final Task successor1 = new Task(executor, runnableB);
		successor1.waitFor(task);
		
		final Task successor2 = new Task(executor, runnableB);
		successor2.waitFor(task);
		
		task.schedule();
		successor1.schedule();
		successor2.schedule();
		successor1.waitForCompletion();
		successor2.waitForCompletion();
	}
	
	@Test
	public void testOrdering() throws InterruptedException, ExecutionException {
		// This test requires executor which many threads.
		final Executor asyncExecutor = Executors.newFixedThreadPool(4);
		
		final List<String> output = Collections.synchronizedList(Lists.<String>newArrayList());
		final Task tasks[] = {
			new Task(asyncExecutor).addWorker(new Sleeper("a", 100L, output)),
			new Task(asyncExecutor).addWorker(new Sleeper("b1", 100L, output)),
			new Task(asyncExecutor).addWorker(new Sleeper("b2", 150L, output)),
			new Task(asyncExecutor).addWorker(new Sleeper("c", 100L, output))
		};
		tasks[3].waitFor(tasks[1], tasks[2]);
		tasks[1].waitFor(tasks[0]);
		tasks[2].waitFor(tasks[0]);
		
		for (final Task task : tasks) {
			task.schedule();
		}
		
		tasks[3].waitForCompletion();
		
		assertEquals("ba", output.get(0));
		assertEquals("ea", output.get(1));
		assertTrue(output.get(2), "bb1".equals(output.get(2)) || "bb2".equals(output.get(2)));
		assertTrue(output.get(3), "bb1".equals(output.get(3)) || "bb2".equals(output.get(3)));
		assertFalse(output.get(2).equals(output.get(3)));
		assertEquals("eb1", output.get(4));
		assertEquals("eb2", output.get(5));
		assertEquals("bc", output.get(6));
		assertEquals("ec", output.get(7));
	}
	
	private static class Sleeper implements Runnable {
		private final String name;
		private final long sleepMs;
		private final List<String> output;
		public Sleeper(final String name, final long sleepMs, final List<String> output) {
			this.name = name;
			this.sleepMs = sleepMs;
			this.output = output;
		}
		
		@Override
		public void run() {
			output.add("b" + name);
			try { Thread.sleep(sleepMs); } catch (final Exception e) {}
			output.add("e" + name);
		}
	}
}
