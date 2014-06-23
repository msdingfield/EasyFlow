package msdingfield.easyflow.reflect;

import static org.junit.Assert.*;
import msdingfield.easyflow.annotations.Aggregate;
import msdingfield.easyflow.annotations.Output;

import org.junit.Test;

public class FieldOperationOutputPortTest {

	protected String unannotatedTestField = "test";
	
	@Output(connectedEdgeName="anotherName")
	protected String aliasedTestField = "test";
	
	@Aggregate
	protected String aggregateTestField = "test";
	
	@Test
	public void testUnannotatedField() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final FieldOperationOutputPort output = new FieldOperationOutputPort(getClass().getDeclaredField("unannotatedTestField"));
		assertFalse(output.aggregate());
		assertEquals("unannotatedTestField", output.getConnectedEdgeName());
		assertEquals("test", output.get(this));
	}

	@Test
	public void testAliasedField() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final FieldOperationOutputPort output = new FieldOperationOutputPort(getClass().getDeclaredField("aliasedTestField"));
		assertFalse(output.aggregate());
		assertEquals("anotherName", output.getConnectedEdgeName());
		assertEquals("test", output.get(this));
	}

	@Test
	public void testAggregateField() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final FieldOperationOutputPort output = new FieldOperationOutputPort(getClass().getDeclaredField("aggregateTestField"));
		assertTrue(output.aggregate());
		assertEquals("aggregateTestField", output.getConnectedEdgeName());
		assertEquals("test", output.get(this));
	}

}
