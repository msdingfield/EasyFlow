package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;

import org.junit.Test;

public class ClassOperationTest {

	public static Executor executor = new Executor() {
		@Override public void execute(final Runnable runnable) {
			runnable.run();
		}
	};

	private static final class BasicRuntimeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	};

	/*
	 * Test a single operation which runs successfully.
	 */
	public static class BasicOperation {
		public static boolean isExecuted = false;
		public void op() {
			isExecuted = true;
		}
	}

	@Test
	public void testBasicOperation() throws NoSuchMethodException, SecurityException, InterruptedException {
		final Task task = makeTask(BasicOperation.class);
		BasicOperation.isExecuted = false;
		runToCompletion(task);
		assertTrue(task.isSuccess());
		assertTrue(BasicOperation.isExecuted);
	}

	/*
	 * Test a single operation which throws an exception.
	 */
	public static class ThrowsOperation
	{
		public void op() {
			throw new BasicRuntimeException();
		}
	}

	@Test
	public void testThrowsOperation() throws NoSuchMethodException, SecurityException, InterruptedException {
		final Task task = makeTask(ThrowsOperation.class);
		runToCompletion(task);
		assertFalse(task.isSuccess());
		assertEquals(1, task.getErrors().size());
		final Throwable error = task.getErrors().iterator().next();
		assertTrue(error instanceof Task.FatalErrorException);
		assertTrue(error.getCause() instanceof BasicRuntimeException);
	}

	/*
	 * Test a single operation which throws exception from constructor.
	 */
	public static class ThrowsInConstructorOperation
	{
		public ThrowsInConstructorOperation() {
			throw new BasicRuntimeException();
		}

		public void op() {
		}
	}

	@Test
	public void testThrowsInConstructorOperation() throws NoSuchMethodException, SecurityException, InterruptedException {
		final Task task = makeTask(ThrowsInConstructorOperation.class);
		runToCompletion(task);
		assertFalse(task.isSuccess());
		assertEquals(1, task.getErrors().size());
		final Throwable error = task.getErrors().iterator().next();
		assertTrue(error instanceof Task.FatalErrorException);
		assertTrue(error.getCause() instanceof BasicRuntimeException);
	}

	/*
	 * Test failure cascading.
	 */
	@Test
	public void testFailureCascading() throws NoSuchMethodException, InterruptedException {
		final Task throwsTask = makeTask(ThrowsOperation.class);
		final Task basicTask = makeTask(BasicOperation.class);
		// Invert dependencies from partial success case
		basicTask.waitFor(throwsTask);
		basicTask.schedule();
		throwsTask.schedule();
		basicTask.join();
		throwsTask.join();

		assertTrue(throwsTask.isComplete());
		assertFalse(throwsTask.isSuccess());
		final Throwable throwsError = throwsTask.getErrors().iterator().next().getCause();
		assertTrue(throwsError instanceof BasicRuntimeException);

		assertTrue(basicTask.isComplete());
		assertFalse(basicTask.isSuccess());
		final Throwable basicError = basicTask.getErrors().iterator().next();
		assertTrue(basicError instanceof Task.DependencyFailureException);
		assertTrue(throwsTask == ((Task.DependencyFailureException)basicError).getPredecessor());
	}

	/*
	 * Test partial success.
	 */
	@Test
	public void testPartialSuccess() throws NoSuchMethodException, InterruptedException {
		final Task throwsTask = makeTask(ThrowsOperation.class);
		final Task basicTask = makeTask(BasicOperation.class);
		// Invert dependencies from cascading failure case
		throwsTask.waitFor(basicTask);
		basicTask.schedule();
		throwsTask.schedule();
		basicTask.join();
		throwsTask.join();

		assertTrue(throwsTask.isComplete());
		assertFalse(throwsTask.isSuccess());
		final Throwable throwsError = throwsTask.getErrors().iterator().next().getCause();
		assertTrue(throwsError instanceof BasicRuntimeException);

		assertTrue(basicTask.isComplete());
		assertTrue(basicTask.isSuccess());
	}

	private void runToCompletion(final Task task)
			throws InterruptedException {
		task.schedule().join();
	}

	private Task makeTask(final Class<?> clazz) throws NoSuchMethodException {
		final ClassOperation op = new ClassOperation();
		op.setOperationClass(clazz);
		op.setConstructor(clazz.getDeclaredConstructor());
		op.setOperationMethod(clazz.getDeclaredMethod("op"));
		final Task task = ClassOperationTaskFactory.create(executor, op, new Context());
		return task;
	}
}
