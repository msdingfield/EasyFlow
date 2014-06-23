package msdingfield.easyflow.reflect;

/** Base interface for inputs and outputs of an operation. */
public interface OperationPort {
	
	/** Return the value type of the port. */
	Class<?> getType();
	
	/** Return the name of the edge which should be connected to this port. */
	String getConnectedEdgeName();
}