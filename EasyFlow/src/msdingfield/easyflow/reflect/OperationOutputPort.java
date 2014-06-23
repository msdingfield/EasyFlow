package msdingfield.easyflow.reflect;

/** Interface for outputs from operation. */
public interface OperationOutputPort extends OperationPort {
	
	/** Get the value from the output port. */
	Object get(final Object instance) throws IllegalArgumentException, IllegalAccessException;
	
	/** 
	 * Determines if multiple outputs to the same edge should be aggregated 
	 * into a collection type.
	 * 
	 * This may only be meaningful in the presence of a fork as multiple nodes
	 * are not allow to output to the same edge.
	 * 
	 * @return True if values should be aggregated.
	 */
	boolean aggregate();
}
