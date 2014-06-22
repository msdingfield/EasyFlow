package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import msdingfield.easyflow.graph.FlowGraph;
import msdingfield.easyflow.graph.FlowGraphTaskBuilder;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TestAnnotations {
	static Class<?>[] c = {TaskA.class};
	
	@Test
	public void test() throws InterruptedException, ExecutionException, IOException {
		final List<ClassOperation> ops = ClassPathScannerFlowGraphBuilder.loadOperationsOnClasspath("msdingfield.easyflow.reflect", "annotationTest");
		FlowGraph<ClassOperationFlowNode> graph = new FlowGraph<ClassOperationFlowNode>(Sets.newHashSet(Lists.transform(ops, new Function<ClassOperation, ClassOperationFlowNode>(){
			@Override public ClassOperationFlowNode apply(ClassOperation arg0) {
				return new ClassOperationFlowNode(arg0);
			}})));
		Context context = new Context();
		context.putPortValue("input", 1);
		
		FlowGraphTaskBuilder
		.graph(graph)
		.taskFactory(ClassOperationTaskFactory.get(context))
		.build()
		.schedule().waitForCompletion();
		
		assertEquals(3, context.getPortValue("a"));
		assertEquals(6, context.getPortValue("b"));
	}

}
