package msdingfield.easyflow.graph;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class GraphTestBase {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	protected Graph<TestNode> graph = null;

	public GraphTestBase() {
		super();
	}

	@Before
	public void setup() {
		try {
			graph = new Graph<>(nodeSet());
		} catch(final Exception e) {
			/* empty */
		}
	}

	abstract Set<TestNode> nodeSet();

}