package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.testsupport.TestExecutor;
import msdingfield.easyflow.testsupport.TimerFuture;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;
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
	public void testOutputFuture() throws NoSuchMethodException, SecurityException, NoSuchFieldException, InterruptedException {
		final ClassOperation futureTask = AnnotationClassOperationBuilder.fromClass(FutureTask.class);
		final ClassOperation consumer = AnnotationClassOperationBuilder.fromClass(Consumer.class);

		final Context context = new Context();
		final Task task1 = ClassOperationTaskFactory.create(executor, futureTask, context);
		final Task task2 = ClassOperationTaskFactory.create(executor, consumer, context);

		task2.waitFor(task1);
		task1.schedule();
		task2.schedule();
		task2.join();

		assertEquals("Yo", context.getEdgeValue("decodedMessage"));
	}

	public static class CollectionOfFuturesTask {
		@Output
		public List<ListenableFuture<String>> messages;

		@Operation
		public void enact() {
			messages = Lists.newArrayList();
			messages.add(new TimerFuture<String>(1000L, "M1"));
			messages.add(new TimerFuture<String>(1000L, "M2"));
		}
	}

	public static class CollectionConsumer {
		@Input
		public List<String> messages;

		@Output
		public List<String> passThru;

		@Operation
		public void enact() {
			passThru = messages;
		}
	}

	@Test
	public void testOutputCollectionOfFutures() throws InterruptedException {
		final ClassOperation futureTask = AnnotationClassOperationBuilder.fromClass(CollectionOfFuturesTask.class);
		final ClassOperation consumer = AnnotationClassOperationBuilder.fromClass(CollectionConsumer.class);

		final Context context = new Context();
		final Task task1 = ClassOperationTaskFactory.create(executor, futureTask, context);
		final Task task2 = ClassOperationTaskFactory.create(executor, consumer, context);

		task2.waitFor(task1);
		task1.schedule();
		task2.schedule();

		task1.join(2000L);
		task2.join(2000L);

		assertTrue(task1.isSuccess());
		assertTrue(task2.isSuccess());

		@SuppressWarnings("unchecked")
		final
		List<String> messages = (List<String>) context.getEdgeValue("passThru");
		assertEquals(2, messages.size());
		assertTrue(messages.contains("M1"));
		assertTrue(messages.contains("M2"));
	}
}
