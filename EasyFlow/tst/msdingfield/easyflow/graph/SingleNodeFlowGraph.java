package msdingfield.easyflow.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class SingleNodeFlowGraph extends FlowGraphTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		final List<Object[]> params = Lists.newArrayList();
		
		params.add(new Object[] {
				new TestNode("a")
		});
		
		params.add(new Object[] {
				new TestNode("a").withInput("in")
		});

		params.add(new Object[] {
				new TestNode("a").withOutput("out")
		});
		
		params.add(new Object[] {
				new TestNode("a").withInput("in").withOutput("out")
		});

		params.add(new Object[] {
				new TestNode("a").withInput("in", "in2").withOutput("out", "out2")
		});
		return params;
	}
	
	@Parameter(0)
	public TestNode node;

	@Override
	Set<TestNode> nodeSet() {
		return Sets.newHashSet(node);
	}

	@Test
	public void testAllNodes() {
		assertEquals(1, graph.getAllNodes().size());
		assertTrue(graph.getAllNodes().contains(new TestNode("a")));
	}
	
	@Test
	public void testGetDirectPredecessorsOfUknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getDirectPredecessors(new TestNode("unknown"));
	}

	@Test
	public void testGetDirectPredecessorsOfNode() {
		final Set<TestNode> pred = graph.getDirectPredecessors(new TestNode("a"));
		assertTrue(pred.isEmpty());
	}
	
	@Test
	public void testGetTransitivePredecessorsOfUknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getTransitivePredecessors(new TestNode("unknown"));
	}

	@Test
	public void testGetTransitivePredecessorsOfNode() {
		final Set<TestNode> pred = graph.getTransitivePredecessors(new TestNode("a"));
		assertTrue(pred.isEmpty());
	}
	
	@Test
	public void testGetProducerSetForUnknownOutput() {
		exception.expect(OutputNotFoundException.class);
		graph.getTransitiveProducerSet("unused");
	}
	
	@Test
	public void testGetProducerSetForKnownOutput() {
		assumeTrue(node.getOutputs().contains("out"));
		final Set<TestNode> prod = graph.getTransitiveProducerSet("out");
		assertEquals(1, prod.size());
		assertTrue(prod.contains(new TestNode("a")));
	}
	
	@Test
	public void testGetDirectSuccessorsOfUnknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getDirectSuccessors(new TestNode("unknown"));
	}
	
	@Test
	public void testGetDirectSuccessorsOfNode() {
		final Set<TestNode> succ = graph.getDirectSuccessors(new TestNode("a"));
		assertTrue(succ.isEmpty());
	}
	
	@Test
	public void testGetSubGraphForEmptyOutputSet() {
		final FlowGraph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet());
		assertTrue(subgraph.getAllNodes().isEmpty());
	}
	
	@Test
	public void testGetSubGraphForUnknownOutput() {
		exception.expect(OutputNotFoundException.class);
		graph.getSubGraphForOutputs(Sets.newHashSet("unknown", "out"));
	}
	
	@Test
	public void testGetSubGraphForKnownOutput() {
		assumeTrue(node.getOutputs().contains("out"));
		final FlowGraph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet("out"));
		assertEquals(1, subgraph.getAllNodes().size());
		assertTrue(subgraph.getAllNodes().contains(new TestNode("a")));
	}
}
