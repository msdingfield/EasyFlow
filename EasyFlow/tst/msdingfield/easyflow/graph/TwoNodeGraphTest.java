package msdingfield.easyflow.graph;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import msdingfield.easyflow.graph.support.NodeNotFoundException;
import msdingfield.easyflow.graph.support.OutputNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(Parameterized.class)
public class TwoNodeGraphTest extends GraphTestBase {

	@Parameters
	public static Collection<Object[]> parameters() {
		final List<Object[]> params = Lists.newArrayList();
		
		// Disconnected
		params.add(new Object[]{
				new TestNode("a").withOutput("aout"),
				new TestNode("b").withInput("bin").withOutput("bout")
		});
		
		// Connected a => b
		params.add(new Object[]{
				new TestNode("a").withOutput("aout", "port"),
				new TestNode("b").withInput("bin", "port").withOutput("bout")
		});
		
		return params;
	}
	
	@Parameter(0)
	public TestNode nodeA;
	
	@Parameter(1)
	public TestNode nodeB;
	
	@Override
	Set<TestNode> nodeSet() {
		return Sets.newHashSet(nodeA, nodeB);
	}

	@Test
	public void testAllNodes() {
		assertEquals(2, graph.getAllNodes().size());
		assertTrue(graph.getAllNodes().contains(new TestNode("a")));
		assertTrue(graph.getAllNodes().contains(new TestNode("b")));
	}
	
	@Test
	public void testDirectPredecessorForUnknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getDirectPredecessors(new TestNode("unknown"));
	}
	
	@Test
	public void testDirectPredecessorForNodeWithoutPredecessors() {
		final Set<TestNode> pred = graph.getDirectPredecessors(new TestNode("a"));
		assertTrue(pred.isEmpty());
	}

	@Test
	public void testDirectPredecessorForNodeWithOnePredecessors() {
		final Set<String> connections = Sets.intersection(nodeA.getOutputs(), nodeB.getInputs());
		assumeFalse(connections.isEmpty());
		
		final Set<TestNode> pred = graph.getDirectPredecessors(new TestNode("b"));
		assertEquals(1, pred.size());
		assertTrue(pred.contains(new TestNode("a")));
	}

	@Test
	public void testTransitivePredecessorForUnknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getTransitivePredecessors(new TestNode("unknown"));
	}
	
	@Test
	public void testTransitivePredecessorForNodeWithoutPredecessors() {
		final Set<TestNode> pred = graph.getTransitivePredecessors(new TestNode("a"));
		assertTrue(pred.isEmpty());
	}

	@Test
	public void testTransitivePredecessorForNodeWithOnePredecessors() {
		final Set<String> connections = Sets.intersection(nodeA.getOutputs(), nodeB.getInputs());
		assumeFalse(connections.isEmpty());
		
		final Set<TestNode> pred = graph.getTransitivePredecessors(new TestNode("b"));
		assertEquals(1, pred.size());
		assertTrue(pred.contains(new TestNode("a")));
	}
	
	@Test
	public void testProducerSetForUnknownOutput() {
		exception.expect(OutputNotFoundException.class);
		graph.getTransitiveProducerSet("unknown");
	}
	
	@Test
	public void testProducerSetForKnownOutput() {
		final Set<TestNode> prod = graph.getTransitiveProducerSet("bout");
		assertTrue(prod.contains(new TestNode("b")));
	}
	
	@Test
	public void testProducerSetForOutputWithMultipleNodes() {
		assumeTrue(isConnectedGraph());

		final Set<TestNode> prod = graph.getTransitiveProducerSet("bout");
		assertEquals(2, prod.size());
		assertTrue(prod.contains(new TestNode("a")));
	}
	
	@Test
	public void testSubGraphForEmptyOutputSet() {
		final Graph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet());
		assertTrue(subgraph.getAllNodes().isEmpty());
	}
	
	@Test
	public void testSubGraphForUnknownOutput() {
		exception.expect(OutputNotFoundException.class);
		graph.getSubGraphForOutputs(Sets.<String>newHashSet("unknown"));
	}
	
	@Test
	public void testSubGraphForOutputWithOneInputNode() {
		final Graph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet("aout"));
		assertEquals(1, subgraph.getAllNodes().size());
		assertTrue(subgraph.getAllNodes().contains(new TestNode("a")));
	}

	@Test
	public void testSubGraphForTwoOutputs() {
		final Graph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet("aout", "bout"));
		assertEquals(2, subgraph.getAllNodes().size());
		assertTrue(subgraph.getAllNodes().contains(new TestNode("a")));
		assertTrue(subgraph.getAllNodes().contains(new TestNode("b")));
	}

	@Test
	public void testSubGraphForOutputWithTwoInputNodes() {
		assumeTrue(isConnectedGraph());
		
		final Graph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet("bout"));
		assertEquals(2, subgraph.getAllNodes().size());
		assertTrue(subgraph.getAllNodes().contains(new TestNode("a")));
		assertTrue(subgraph.getAllNodes().contains(new TestNode("b")));
	}

	private boolean isConnectedGraph() {
		final Set<String> connections = Sets.intersection(nodeA.getOutputs(), nodeB.getInputs());
		return connections.size() > 0;
	}
	
}
