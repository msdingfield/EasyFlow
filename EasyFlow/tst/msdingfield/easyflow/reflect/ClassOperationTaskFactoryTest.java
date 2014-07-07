package msdingfield.easyflow.reflect;

import static org.junit.Assert.*;
import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.execution.DefaultExecutor;
import msdingfield.easyflow.execution.Task;

import org.junit.Test;

public class ClassOperationTaskFactoryTest {

	@Test
	public void test() throws InterruptedException {
		final Context context = new Context();
		final ClassOperationTaskFactory factory = new ClassOperationTaskFactory(context);
		final Task task = factory.create(DefaultExecutor.get(), ClassOperationFlowNode.toFlowNode(AnnotationClassOperationBuilder.fromClass(TestOp.class)));
		
		context.setEdgeValue("input", 3);
		task.schedule().join();
		final int output = (int) context.getEdgeValue("output");
		assertEquals(6, output);
	}
	
	public static class TestOp {
		
		@Input
		public int input = 0;
		
		@Output
		public int output = 0;
		
		@Operation
		public void op() {
			output = 2 * input;
		}
	}
}
