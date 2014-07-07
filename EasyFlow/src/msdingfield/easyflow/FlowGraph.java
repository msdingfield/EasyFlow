package msdingfield.easyflow;

import java.util.Map;

/** A FlowGraph that can be evaluated. */
public interface FlowGraph {

	/**
	 * Evaluates this FlowGraph asynchronously.
	 * 
	 * @param params Parameters to the evaluation.
	 * @return A task for querying the state of the running evaluation.
	 */
	FlowEvaluation evaluate(final Map<String, Object> params);
}
