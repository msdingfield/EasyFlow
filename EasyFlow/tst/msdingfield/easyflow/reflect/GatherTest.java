package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import msdingfield.easyflow.annotations.Aggregate;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.testsupport.TestExecutor;

import org.junit.Rule;
import org.junit.Test;

public class GatherTest {

	@Rule
	public TestExecutor executor = new TestExecutor();
	
	public static class Producer1 {
		
		@Aggregate
		@Output(port="names")
		public String name;
		
		@Operation
		public void enact() {
			name = "Producer1";
		}
	}
	
	public static class Producer2 {
		@Aggregate
		@Output(port="names")
		public String name;
		
		@Operation
		public void enact() {
			name ="Producer2";
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		final Context context = new Context();
		ClassOperation op = AnnotationClassOperationBuilder.fromClass(Producer1.class);
		final Task task1 = ClassOperationTaskFactory.create(executor, op, context);

		ClassOperation op1 = AnnotationClassOperationBuilder.fromClass(Producer2.class);
		final Task task2 = ClassOperationTaskFactory.create(executor, op1, context);
		
		task1.schedule();
		task1.waitForCompletion();
		task2.schedule();
		task2.waitForCompletion();
		@SuppressWarnings("unchecked")
		final Collection<String> names = (Collection<String>) context.getPortValue("names");
		assertEquals(2, names.size());
		assertTrue(names.contains("Producer1"));
		assertTrue(names.contains("Producer2"));
	}
}
