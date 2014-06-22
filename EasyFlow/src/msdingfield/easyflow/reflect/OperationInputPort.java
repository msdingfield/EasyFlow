package msdingfield.easyflow.reflect;

public interface OperationInputPort extends OperationPort {
	Object get(final Object instance) throws IllegalArgumentException, IllegalAccessException;
	boolean aggregate();
}
