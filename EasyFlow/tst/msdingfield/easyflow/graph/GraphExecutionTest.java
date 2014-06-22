package msdingfield.easyflow.graph;

import static msdingfield.easyflow.graph.DiamondOperations.a;
import static msdingfield.easyflow.graph.DiamondOperations.b1;
import static msdingfield.easyflow.graph.DiamondOperations.b2;
import static msdingfield.easyflow.graph.DiamondOperations.c;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.graph.FlowGraph;
import msdingfield.easyflow.graph.FlowGraphTaskBuilder;
import msdingfield.easyflow.graph.TaskFactory;
import msdingfield.easyflow.reflect.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(Parameterized.class)
public class GraphExecutionTest {

	public static class Params {
		public Set<AddOperation> ops = null;
		
		public String inputKey = null;
		public Integer inputValue = null;
		
		public Integer aValue = null;
		public Integer b1Value = null;
		public Integer b2Value = null;
		public Integer cValue = null;
	}

	@Parameters
	public static ArrayList<Params[]> getParamStructs() {
		ArrayList<Params[]> paramArray = Lists.newArrayList();
		for (final Params test : tests) {
			paramArray.add(new Params[] {test});
		}
		return paramArray;
	}
	
	public GraphExecutionTest(final Params param) {
		params = param;
	}

	private Params params = null;

	public static Params[] tests = {
		
		// Empty System
		new Params(){{
			ops = Sets.newHashSet();
			inputKey = "a";
			inputValue = 99;
			aValue = 99;
		}},
		
		// One Operation
		new Params(){{
			ops = Sets.newHashSet(a);
			inputKey = "input";
			inputValue = 1;
			aValue = 3;
		}},
		
		// Two Operations Linear
		new Params(){{
			ops = Sets.newHashSet(a, b1);
			inputKey = "input";
			inputValue = 1;
			aValue = 3;
			b1Value = 7;
		}},
		
		// Two Operations Independent
		new Params(){{
			ops = Sets.newHashSet(b2, b1);
			inputKey = "a";
			inputValue = 1;
			aValue = 1;
			b1Value = 5;
			b2Value = 9;
		}},
		
		// Fork
		new Params(){{
			ops = Sets.newHashSet(a, b2, b1);
			inputKey = "input";
			inputValue = 1;
			aValue = 3;
			b1Value = 7;
			b2Value = 11;
		}},
		
		// Join
		new Params(){{
			ops = Sets.newHashSet(c, b2, b1);
			inputKey = "a";
			inputValue = 1;
			aValue = 1;
			b1Value = 5;
			b2Value = 9;
			cValue = 30;
		}},
		
		// Diamond
		new Params(){{
			ops = Sets.newHashSet(a, c, b2, b1);
			inputKey = "input";
			inputValue = 1;
			aValue = 3;
			b1Value = 7;
			b2Value = 11;
			cValue = 34;
		}}
	};
	
	private Context context = new Context();
	
	@Before
	public void setup() {
		context = new Context();
	}
	
	@Test
	public void test() throws InterruptedException {
		final FlowGraph<AddOperation> graph = new FlowGraph<AddOperation>(params.ops);
		
		context.putPortValue(params.inputKey, params.inputValue);
		FlowGraphTaskBuilder
		.graph(graph)
		.taskFactory(AddOperation.getTaskFactory(context))
		.build()
		.schedule().waitForCompletion();
		
		assertEquals(params.aValue, context.getPortValue("a"));
		assertEquals(params.b1Value, context.getPortValue("b.1"));
		assertEquals(params.b2Value, context.getPortValue("b.2"));
		assertEquals(params.cValue, context.getPortValue("c"));
	}
	
	public static final class AddOperationTaskFactory implements TaskFactory<AddOperation> {
		private Context context;
		public AddOperationTaskFactory(final Context context) {
			this.context = context;
		}
		
		@Override
		public Task create(final Executor executor, final AddOperation op) {
			final Task task = new Task(executor);
			task.addWorker(new Runnable(){
				@Override public void run() {
					op.execute(context);
				}});
			return task;
		}

	}
}
