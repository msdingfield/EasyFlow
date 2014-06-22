package msdingfield.easyflow.graph;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class FlowGraphTestBase {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	protected FlowGraph<TestNode> graph = null;

	public FlowGraphTestBase() {
		super();
	}

	@Before
	public void setup() {
		try {
			graph = new FlowGraph<>(nodeSet());
		} catch(final Exception e) {
			/* empty */
		}
	}

	abstract Set<TestNode> nodeSet();

}