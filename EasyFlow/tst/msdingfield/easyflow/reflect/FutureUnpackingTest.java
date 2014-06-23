package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.testsupport.TestExecutor;
import msdingfield.easyflow.testsupport.TimerFuture;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.util.concurrent.ListenableFuture;

public class FutureUnpackingTest {

	@Rule
	public TestExecutor executor = new TestExecutor();
	
	public static class FutureTask {
		@Output
		public ListenableFuture<String> message;
		
		@Operation
		public void enact() {
			message = new TimerFuture<String>(1000L, "Yo");
		}
	}
	
	public static class Consumer {
		@Input
		public String message;
		
		@Output
		public String decodedMessage;
		
		@Operation
		public void enact() {
			decodedMessage = message;
		}
	}
	
	@Test
	public void test() throws NoSuchMethodException, SecurityException, NoSuchFieldException, InterruptedException {
		final ClassOperation futureTask = AnnotationClassOperationBuilder.fromClass(FutureTask.class);
		final ClassOperation consumer = AnnotationClassOperationBuilder.fromClass(Consumer.class);
		
		final Context context = new Context();
		final Task task1 = ClassOperationTaskFactory.create(executor, futureTask, context);
		final Task task2 = ClassOperationTaskFactory.create(executor, consumer, context);
		
		task2.waitFor(task1);
		task1.schedule();
		task2.schedule();
		task2.waitForCompletion();
		
		assertEquals("Yo", context.getEdgeValue("decodedMessage"));
	}
}
