package msdingfield.easyflow.execution;

import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;

public class MonitorTestBase {
	
	@Rule public JUnitRuleMockery context = new JUnitRuleMockery();
	@Mock public Runnable run1;
	@Mock public Runnable run2;
	
	protected Monitor m = null;
	
	@Before
	public void setup() {
		m = new Monitor();
	}
}
