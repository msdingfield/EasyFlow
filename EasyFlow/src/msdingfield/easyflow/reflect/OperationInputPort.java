package msdingfield.easyflow.reflect;

/** Interface for inputs to an operation. */
public interface OperationInputPort extends OperationPort {

	/** Set the value of the input. */
	public void set(final Object instance, final Object value) throws IllegalArgumentException, IllegalAccessException;
	

	/** Determine if we should parallelize on collection value.
	 * 
	 * If the incoming edge value is a collection, determine if we should 
	 * process each member in parallel.
	 */
	public boolean fork();
}
