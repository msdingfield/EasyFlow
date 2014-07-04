package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;
import msdingfield.easyflow.testsupport.TestExecutor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AnnotationClassOperationBuilderTest {

	@Rule
	public TestExecutor executor = new TestExecutor();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	public static class NoOperation {

	}

	public static class OperationNoParams {
		public static boolean enacted = false;
		@Operation
		public void enact() {
			enacted = true;
		}
	}

	public static class OperationWithInputAttribute {
		public static String receivedInput = "";

		@Input
		public String myInput;

		@Operation
		public void enact() {
			receivedInput = myInput;
		}
	}

	public static class OperationWithOutputAttribute {

		@Output
		public String myOutput;

		@Operation
		public void enact() {
			myOutput = "foobar";
		}
	}

	public static class OperationWithInputOutput {

		@Input
		public String myInput;

		@Output
		public String myOutput;

		@Operation
		public void enact() {
			myOutput = myInput;
		}

	}

	@Test
	public void testNoOperationAnnotation() {
		exception.expect(InvalidOperationBindingException.class);
		AnnotationClassOperationBuilder.fromClass(NoOperation.class);
	}

	@Test
	public void testOperationWithNoParameters() {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(OperationNoParams.class);
		assertEquals(0, op.getInputs().size());
		assertEquals(0, op.getOutputs().size());

		OperationNoParams.enacted = false;
		final Context context = new Context();
		runOperation(op, context);
		assertTrue(OperationNoParams.enacted);
	}

	@Test
	public void testOperationWithInput() {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(OperationWithInputAttribute.class);
		assertEquals(1, op.getInputs().size());
		assertEquals(0, op.getOutputs().size());

		final ClassOperationFlowNode node = ClassOperationFlowNode.toFlowNode(op);
		assertTrue(node.getInputs().contains("myInput"));

		final Context context = new Context();
		context.setEdgeValue("myInput", "foobar");
		OperationWithInputAttribute.receivedInput = "";
		runOperation(op, context);
		assertEquals("foobar", OperationWithInputAttribute.receivedInput);
	}

	@Test
	public void testOperationWithOutput() {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(OperationWithOutputAttribute.class);
		assertEquals(0, op.getInputs().size());
		assertEquals(1, op.getOutputs().size());

		final ClassOperationFlowNode node = ClassOperationFlowNode.toFlowNode(op);
		assertTrue(node.getOutputs().contains("myOutput"));

		final Context context = new Context();
		runOperation(op, context);
		assertEquals("foobar", context.getEdgeValue("myOutput"));
	}

	@Test
	public void testOperationWithInputOutput() {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(OperationWithInputOutput.class);
		assertEquals(1, op.getInputs().size());
		assertEquals(1, op.getOutputs().size());

		final ClassOperationFlowNode node = ClassOperationFlowNode.toFlowNode(op);
		assertTrue(node.getInputs().contains("myInput"));

		assertTrue(node.getOutputs().contains("myOutput"));

		final Context context = new Context();
		context.setEdgeValue("myInput", "raboof");
		runOperation(op, context);
		assertEquals("raboof", context.getEdgeValue("myOutput"));
	}

	private void runOperation(final ClassOperation op, final Context context) {
		final Task task = ClassOperationTaskFactory.create(executor, op, context);
		task.schedule();
		try {
			task.join();
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
