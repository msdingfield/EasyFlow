package msdingfield.easyflow.graph;

import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.DefaultExecutor;
import msdingfield.easyflow.execution.Task;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Sets;

public class FlowGraphTaskBuilderTest {

	@Rule
	public JUnitRuleMockery mockery = new JUnitRuleMockery();
	
	private FlowGraph<TestNode> graph = new FlowGraph<TestNode>(
			Sets.<TestNode>newHashSet(
					new TestNode("a").withOutput("e"), 
					new TestNode("b").withInput("e")));

	private TaskFactory<TestNode> factory;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		factory = mockery.mock(TaskFactory.class);
	}
	
	@Test
	public void testDefaultExecutor() throws InterruptedException {
		
		mockery.checking(new Expectations(){{
			oneOf(factory).create(DefaultExecutor.get(), new TestNode("a"));
			will(returnValue(new Task(DefaultExecutor.get())));
			
			oneOf(factory).create(DefaultExecutor.get(), new TestNode("b"));
			will(returnValue(new Task(DefaultExecutor.get()))); 
		}});
		
		FlowGraphTaskBuilder
			.graph(graph)
			.taskFactory(factory)
			.build();
	}

	@Test
	public void testExecutor() throws InterruptedException {
		final Runnable runA = mockery.mock(Runnable.class, "a");
		final Runnable runB = mockery.mock(Runnable.class, "b");
		
		final Executor executor = new Executor() {
			@Override public void execute(final Runnable command) {
				command.run();
			}};
		
		mockery.checking(new Expectations(){{
			oneOf(factory).create(executor, new TestNode("a"));
			will(returnValue(new Task(executor, runA)));
			
			oneOf(factory).create(executor, new TestNode("b"));
			will(returnValue(new Task(executor, runB)));

			final Sequence seq = mockery.sequence("seq");
			oneOf(runA).run(); inSequence(seq);
			oneOf(runB).run(); inSequence(seq);
		}});
		
		final Task task = FlowGraphTaskBuilder
			.graph(graph)
			.taskFactory(factory)
			.executor(executor)
			.build();
		
		task.schedule().join();
	}
}
