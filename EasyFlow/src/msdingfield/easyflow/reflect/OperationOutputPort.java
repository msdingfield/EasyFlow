package msdingfield.easyflow.reflect;

public interface OperationOutputPort extends OperationPort {
	Object get(final Object instance) throws IllegalArgumentException, IllegalAccessException;
	boolean aggregate();
}
