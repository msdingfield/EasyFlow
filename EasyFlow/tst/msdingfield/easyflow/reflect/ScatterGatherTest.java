package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import msdingfield.easyflow.annotations.ForkOn;
import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.testsupport.TestExecutor;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ScatterGatherTest {

	@Rule
	public TestExecutor executor = new TestExecutor();
	
	public static class ScatterGatherOperation {

		@ForkOn
		@Input(connectedEdgeName="numbers")
		public String number;
		
		@Output
		public int integer;
		
		@Operation
		public void enact() {
			integer = Integer.parseInt(number);
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(ScatterGatherOperation.class);
		final Context context = new Context();
		context.setEdgeValue("numbers", Lists.newArrayList("8", "3", "6"));
		final Task task = ClassOperationTaskFactory.create(executor, op, context);
		task.schedule();
		task.waitForCompletion();
		
		@SuppressWarnings("unchecked")
		final Collection<Integer> integers = (Collection<Integer>) context.getEdgeValue("integer");
		assertEquals(3, integers.size());
		assertTrue(integers.contains(3));
		assertTrue(integers.contains(6));
		assertTrue(integers.contains(8));
	}
}
