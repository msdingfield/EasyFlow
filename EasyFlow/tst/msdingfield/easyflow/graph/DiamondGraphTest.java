package msdingfield.easyflow.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.common.collect.Sets;

public class DiamondGraphTest extends GraphTestBase {

	@Override
	Set<TestNode> nodeSet() {
		final TestNode a = new TestNode("a").withOutput("a-out");
		final TestNode b1 = new TestNode("b1").withInput("a-out").withOutput("b1-out");
		final TestNode b2 = new TestNode("b2").withInput("a-out").withOutput("b2-out");
		final TestNode c = new TestNode("c").withInput("b1-out", "b2-out");
		return Sets.newHashSet(a, b1, b2, c);
	}
	
	@Test
	public void testAllNodes() {
		final Set<TestNode> allNodes = graph.getAllNodes();
		assertEquals(4, allNodes.size());
		assertTrue(allNodes.contains(new TestNode("a")));
		assertTrue(allNodes.contains(new TestNode("b1")));
		assertTrue(allNodes.contains(new TestNode("b2")));
		assertTrue(allNodes.contains(new TestNode("c")));
	}
	
	@Test
	public void testDirectPredecessorOfNodeWithNoPredecessor() {
		assertTrue(graph.getDirectPredecessors(new TestNode("a")).isEmpty());
	}
	
	@Test
	public void testDirectPredecessorOfNodeWithOnePredecessor() {
		assertTrue(graph.getDirectPredecessors(new TestNode("b1")).equals(Sets.newHashSet(new TestNode("a"))));
		assertTrue(graph.getDirectPredecessors(new TestNode("b2")).equals(Sets.newHashSet(new TestNode("a"))));
	}
	
	@Test
	public void testDirectPredecessorOfNodeWithTwoPredecessors() {
		assertTrue(graph.getDirectPredecessors(new TestNode("c")).equals(Sets.newHashSet(new TestNode("b1"), new TestNode("b2"))));
	}

	@Test
	public void testSubGraph() throws InterruptedException, ExecutionException {
		final Graph<TestNode> partial = graph.getSubGraphForOutputs(Sets.newHashSet("b1-out", "a-out"));

		final Collection<TestNode> allOperations = partial.getAllNodes();
		assertEquals(2, allOperations.size());
		assertTrue(allOperations.contains(new TestNode("a")));
		assertTrue(allOperations.contains(new TestNode("b1")));
	}

}
