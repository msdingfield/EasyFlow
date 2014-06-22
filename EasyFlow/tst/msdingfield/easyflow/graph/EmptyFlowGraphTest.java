package msdingfield.easyflow.graph;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import msdingfield.easyflow.graph.support.NodeNotFoundException;
import msdingfield.easyflow.graph.support.OutputNotFoundException;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Sets;

public class EmptyFlowGraphTest extends FlowGraphTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		
		final List<Object[]> params = new ArrayList<Object[]>();
		params.add(new Object[] {Collections.<TestNode>emptySet()});
		params.add(new Object[] {Sets.newHashSet(new TestNode("no predecessor"))});
		params.add(new Object[] {Sets.newHashSet(
				new TestNode("no predecessor").withOutput("a"), 
				new TestNode("no successor").withInput("a"))});
		return params;
	}
	
	Set<TestNode> nodeSet() {
		return Sets.newHashSet();
	}
	
	@Test
	public void testConstructGraph() {
		new FlowGraph<TestNode>(nodeSet());
	}
	
	@Test
	public void testEmptyGraphHasNoNodes() {
		assertTrue(graph.getAllNodes().isEmpty());
	}

	@Test
	public void testDirectPredecessorOfUnknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getDirectPredecessors(new TestNode("testUnknownNodeCausesException"));
	}

	@Test
	public void testTransitivePredecessorOfUnknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getTransitivePredecessors(new TestNode("testUnknownNodeCausesException"));
	}
	
	@Test
	public void testTransitiveProducerSet() {
		exception.expect(OutputNotFoundException.class);
		graph.getTransitiveProducerSet("anything");
	}
	
	@Test
	public void testDirectSuccessorOfUnknownNode() {
		exception.expect(NodeNotFoundException.class);
		graph.getDirectSuccessors(new TestNode("testDirectSuccessorOfUnknownNode"));
	}
	
	@Test
	public void testGetSubGraphForEmptyOutputSet() {
		final FlowGraph<TestNode> subgraph = graph.getSubGraphForOutputs(Sets.<String>newHashSet());
		assertTrue(subgraph.getAllNodes().isEmpty());
	}

	@Test
	public void testGetSubGraphOutputSet() {
		exception.expect(OutputNotFoundException.class);
		graph.getSubGraphForOutputs(Sets.<String>newHashSet("anything"));
	}
}
