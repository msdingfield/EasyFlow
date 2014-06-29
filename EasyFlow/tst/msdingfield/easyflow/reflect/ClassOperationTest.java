package msdingfield.easyflow.reflect;

import java.util.concurrent.Executor;

import org.junit.Test;

public class ClassOperationTest {

	public static Executor executor = new Executor() {
		@Override public void execute(final Runnable runnable) {
			runnable.run();
		}};

		public static abstract class TestOperation {
			public static boolean isExecuted = false;
			public void op() {
				isExecuted = true;
			}
		}


		@Test
		public void test() throws NoSuchMethodException, SecurityException, InterruptedException {
			final ClassOperation op = new ClassOperation();
			op.setOperationClass(TestOperation.class);
			op.setConstructor(TestOperation.class.getDeclaredConstructor());
			op.setOperationMethod(TestOperation.class.getDeclaredMethod("op"));
			TestOperation.isExecuted = false;
			//ClassOperationTaskFactory.create(executor, new ClassOperationProxy(op), new Context()).schedule().waitForCompletion();
			//assertTrue(TestOperation.isExecuted);
		}
}
