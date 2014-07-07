package msdingfield.easyflow.execution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

public class MonitorPassTest extends MonitorTestBase {
	
	@Before
	public void setup() {
		super.setup();

		context.checking(new Expectations(){{
			oneOf(run1).run();
			oneOf(run2).run();
		}});

		m.addListener(run1);	
		m.addListener(run2);
		
	}
	
	@Test
	public void testAcquireOnce() {
		assertFalse(m.isLocked());
		m.acquire();
		assertTrue(m.isLocked());
		m.release();
		assertFalse(m.isLocked());
	}
	
	@Test
	public void testAcquireTwice() {
		m.acquire();
		m.acquire();
		m.release();
		m.release();
	}
}