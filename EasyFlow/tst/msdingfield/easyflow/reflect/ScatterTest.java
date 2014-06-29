package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import msdingfield.easyflow.annotations.ForkOn;
import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.testsupport.TestExecutor;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ScatterTest {

	@Rule
	public TestExecutor executor = new TestExecutor();
	
	public static class ScatterOperation {
   		
		@Input
		public Map<Integer,String> mapping;

		@ForkOn
		@Input(connectedEdgeName="numbers")
		public String number;
		
		@Operation
		public void enact() {
			final int n = Integer.parseInt(number);
			mapping.put(n, number);
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		final ClassOperationProxy op = AnnotationClassOperationBuilder.fromClass(ScatterOperation.class);
		final Context context = new Context();
		final HashMap<Integer, String> mapping = new HashMap<Integer,String>();
		context.setEdgeValue("mapping", mapping);
		context.setEdgeValue("numbers", Lists.newArrayList("8", "3", "6"));
		final Task task = ClassOperationTaskFactory.create(executor, op, context);
		task.schedule();
		task.waitForCompletion();
		
		assertEquals(3, mapping.size());
		assertEquals("3", mapping.get(3));
		assertEquals("6", mapping.get(6));
		assertEquals("8", mapping.get(8));
	}
}
