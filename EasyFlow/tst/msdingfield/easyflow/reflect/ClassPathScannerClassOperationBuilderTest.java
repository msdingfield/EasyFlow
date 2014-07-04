package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Scope;
import msdingfield.easyflow.reflect.nestedpackage.CpScanTestNestedOp;

import org.junit.Test;

public class ClassPathScannerClassOperationBuilderTest {

	@Test
	public void test() {
		final List<ClassOperation> ops = ClassPathScannerClassOperationBuilder.loadOperationsOnClasspath("msdingfield.easyflow.reflect", "cpScanTestA");
		assertEquals(3, ops.size());

		assertTrue(ops.contains(AnnotationClassOperationBuilder.fromClass(CpScanTestTopLevelOpA.class)));
		assertTrue(ops.contains(AnnotationClassOperationBuilder.fromClass(InnerClassOpA.class)));
		assertTrue(ops.contains(AnnotationClassOperationBuilder.fromClass(CpScanTestNestedOp.class)));
	}

	@Scope("cpScanTestA")
	public static class InnerClassOpA {
		@Operation
		public void op() {}
	}

	@Scope("cpScanTestB")
	public static class InnerClassOpB {
		@Operation
		public void op() {}
	}

	@Scope("cpScanTestA")
	public static class InnerClassNoOp {
	}
}
