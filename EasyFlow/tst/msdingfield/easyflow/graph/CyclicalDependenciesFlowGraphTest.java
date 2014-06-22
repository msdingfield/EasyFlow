package msdingfield.easyflow.graph;

import java.util.Set;

import msdingfield.easyflow.support.CyclicDependencyException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Sets;

public class CyclicalDependenciesFlowGraphTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		exception.expect(CyclicDependencyException.class);
	}
	
	@Test
	public void testSingleNodeCycle() {
		new FlowGraph<TestNode>(Sets.<TestNode>newHashSet(new TestNode("a").withInput("z").withOutput("z")));
	}
	
	@Test
	public void testDoubleNodeCycle() {
		final Set<TestNode> nodes = Sets.newHashSet();
		nodes.add(new TestNode("a").withInput("input", "e2").withOutput("e1"));
		nodes.add(new TestNode("b").withInput("e1").withOutput("e2", "output"));
		new FlowGraph<TestNode>(nodes);
	}
	
}
