package msdingfield.easyflow.execution;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MonitorFailTest extends MonitorTestBase {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		super.setup();
		context.checking(new Expectations(){{
			never(run1).run();
			never(run2).run();
		}});

		m.addListener(run1);	
		m.addListener(run2);
	}
	
	@Test
	public void testAcquire() {
		m.acquire();
		m.acquire();
	}

	@Test
	public void testUnderflow() {
		exception.expect(Monitor.UnderflowException.class);
		m.release();
	}
	
}