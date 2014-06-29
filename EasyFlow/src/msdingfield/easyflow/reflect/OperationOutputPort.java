package msdingfield.easyflow.reflect;

/** Interface for outputs from operation. */
public interface OperationOutputPort extends OperationPort {

	/** Get the value from the output port. */
	Object get(final Object instance) throws IllegalArgumentException, IllegalAccessException;
}
