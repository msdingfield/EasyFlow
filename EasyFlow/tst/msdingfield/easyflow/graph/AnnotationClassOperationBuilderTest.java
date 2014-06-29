package msdingfield.easyflow.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.reflect.AnnotationClassOperationBuilder;
import msdingfield.easyflow.reflect.ClassOperationProxy;
import msdingfield.easyflow.reflect.OperationInputPort;
import msdingfield.easyflow.reflect.OperationOutputPort;
import msdingfield.easyflow.reflect.OperationPort;
import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

import org.junit.Test;

public class AnnotationClassOperationBuilderTest {

	@Test(expected=InvalidOperationBindingException.class)
	public void testClassWithNoDefaultConstructor() {
		AnnotationClassOperationBuilder.fromClass(NoDefaultConstructor.class);
	}
	
	@Test(expected=InvalidOperationBindingException.class)
	public void testClassWithNoOperation() {
		AnnotationClassOperationBuilder.fromClass(NoOperation.class);
	}
	
	@Test
	public void testClassWithOneOutput() {
		final ClassOperationProxy op = AnnotationClassOperationBuilder.fromClass(OneOutput.class);
		assertTrue(op.getInputs().isEmpty());
		assertEquals(1, op.getOutputs().size());
		assertEquals("output", op.getOutputs().iterator().next().getConnectedEdgeName());
		assertEquals(int.class, op.getOutputs().iterator().next().getType());
	}

	@Test
	public void testClassWithOneInput() {
		final ClassOperationProxy op = AnnotationClassOperationBuilder.fromClass(OneInput.class);
		assertTrue(op.getOutputs().isEmpty());
		assertEquals(1, op.getInputs().size());
		assertEquals("input", op.getInputs().iterator().next().getConnectedEdgeName());
		assertEquals(int.class, op.getInputs().iterator().next().getType());
	}
	
	@Test
	public void testClassWithInputAndOutput() {
		final ClassOperationProxy op = AnnotationClassOperationBuilder.fromClass(InputAndOutput.class);
		
		assertEquals(2, op.getOutputs().size());
		final OperationOutputPort o1 = getPort(op.getOutputs(), "o1");
		assertEquals(int.class, o1.getType());
		
		final OperationOutputPort o2 = getPort(op.getOutputs(), "o2");
		assertEquals(String.class, o2.getType());

		assertEquals(2, op.getInputs().size());
		
		final OperationInputPort i1 = getPort(op.getInputs(), "i1");
		assertEquals(int.class, i1.getType());
		
		final OperationInputPort i2 = getPort(op.getInputs(), "i2");
		assertEquals(String.class, i2.getType());
	}


	private static <T extends OperationPort> T getPort(final Collection<T> ports, final String name) {
		for (final T port : ports) {
			if (name.equals(port.getConnectedEdgeName())) {
				return port;
			}
		}
		return null;
	}


	public static class NoDefaultConstructor {
		public NoDefaultConstructor(final int n) {}
		
		@Operation
		public void op() {}
	}
	
	public static class NoOperation {}
	
	public static class OneOutput {
		
		@Output
		public int output = 0;
		
		@Operation
		public void op() {}
	}

	public static class OneInput {
		
		@Input
		public int input = 0;
		
		@Operation
		public void op() {}
	}
	
	public static class InputAndOutput {
		
		@Input
		public int i1 = 0;
		
		@Input
		public String i2 = "";
		
		@Output
		public int o1 = 0;
		
		@Output
		public String o2 = "";
		
		@Operation
		public void op() {}
	}
}
