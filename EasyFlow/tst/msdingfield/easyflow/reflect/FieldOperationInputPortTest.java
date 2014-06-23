package msdingfield.easyflow.reflect;

import static org.junit.Assert.*;
import msdingfield.easyflow.annotations.ForkOn;
import msdingfield.easyflow.annotations.Input;

import org.junit.Before;
import org.junit.Test;

public class FieldOperationInputPortTest {

	private static final String ANOTHER_NAME = "anotherName";
	private static final String UNANNOTATED_TEST_FIELD = "unannotatedTestField";
	private static final String ALIASED_TEST_FIELD = "aliasedTestField";
	private static final String FORK_ON_TEST_FIELD = "forkOnTestField";
	
	protected String unannotatedTestField = "";
	
	@Input(connectedEdgeName=ANOTHER_NAME)
	protected String aliasedTestField = "";
	
	@ForkOn
	protected String forkOnTestField = "";
	
	@Before
	public void setup() throws NoSuchFieldException, SecurityException {
		unannotatedTestField = "";
		aliasedTestField = "";
		forkOnTestField = "";
	}
	
	@Test
	public void testUnannotatedField() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		final FieldOperationInputPort unannotatedTestFieldInputPort = new FieldOperationInputPort(getClass().getDeclaredField(UNANNOTATED_TEST_FIELD));
		assertFalse(unannotatedTestFieldInputPort.fork());
		assertEquals(UNANNOTATED_TEST_FIELD, unannotatedTestFieldInputPort.getConnectedEdgeName());
		unannotatedTestFieldInputPort.set(this, "test");
		assertEquals("test", unannotatedTestField);
	}
	
	@Test
	public void testAliasedField() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		final FieldOperationInputPort aliasedTestFieldInputPort = new FieldOperationInputPort(getClass().getDeclaredField(ALIASED_TEST_FIELD));
		assertFalse(aliasedTestFieldInputPort.fork());
		assertEquals(ANOTHER_NAME, aliasedTestFieldInputPort.getConnectedEdgeName());
		aliasedTestFieldInputPort.set(this, "test");
		assertEquals("test", aliasedTestField);
	}
	
	@Test
	public void testForkOnField() throws NoSuchFieldException, SecurityException {
		final FieldOperationInputPort forkOnTestFieldInputPort = new FieldOperationInputPort(getClass().getDeclaredField(FORK_ON_TEST_FIELD));
		assertTrue(forkOnTestFieldInputPort.fork());
	}
}
