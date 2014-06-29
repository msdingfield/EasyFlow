package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import msdingfield.easyflow.annotations.Output;

import org.junit.Test;

public class FieldOperationOutputPortTest {

	protected String unannotatedTestField = "test";

	@Output(connectedEdgeName="anotherName")
	protected String aliasedTestField = "test";

	protected String aggregateTestField = "test";

	@Test
	public void testUnannotatedField() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final FieldOperationOutputPort output = new FieldOperationOutputPort(getClass().getDeclaredField("unannotatedTestField"));
		assertEquals("unannotatedTestField", output.getConnectedEdgeName());
		assertEquals("test", output.get(this));
	}

	@Test
	public void testAliasedField() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final FieldOperationOutputPort output = new FieldOperationOutputPort(getClass().getDeclaredField("aliasedTestField"));
		assertEquals("anotherName", output.getConnectedEdgeName());
		assertEquals("test", output.get(this));
	}

}
