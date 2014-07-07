package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;

import org.junit.Test;

import com.google.common.collect.Lists;

public class ClassOperationFlowNodeTest {

	@Test
	public void testClassOperationWithNoInputOutput() {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(NoIO.class);
		final ClassOperationFlowNode node = ClassOperationFlowNode.toFlowNode(op);
		assertTrue(node.getInputs().isEmpty());
		assertTrue(node.getOutputs().isEmpty());
		assertTrue(op == node.getOp());
	}

	@Test
	public void testClassOperationWithInputOutput() {
		final ClassOperation op = AnnotationClassOperationBuilder.fromClass(IO.class);
		final ClassOperationFlowNode node = ClassOperationFlowNode.toFlowNode(op);
		assertEquals(1, node.getInputs().size());
		assertTrue(node.getInputs().contains("input"));

		assertEquals(1, node.getOutputs().size());
		assertTrue(node.getOutputs().contains("output"));
	}

	@Test
	public void testMultipleClasses() {
		final ClassOperationFlowNode op1 = ClassOperationFlowNode.toFlowNode(AnnotationClassOperationBuilder.fromClass(NoIO.class));
		final ClassOperationFlowNode op2 = ClassOperationFlowNode.toFlowNode(AnnotationClassOperationBuilder.fromClass(IO.class));
		final List<ClassOperationFlowNode> ops = ClassOperationFlowNode.toFlowNodes(Lists.newArrayList(
				AnnotationClassOperationBuilder.fromClass(NoIO.class),
				AnnotationClassOperationBuilder.fromClass(IO.class)));
		assertEquals(2, ops.size());
		assertTrue(ops.contains(op1));
		assertTrue(ops.contains(op2));
	}

	public static class NoIO { @Operation public void op(){}}

	public static class IO {
		@Input
		public int input = 0;

		@Output
		public int output = 0;

		@Operation
		public void op() {}
	}
}
