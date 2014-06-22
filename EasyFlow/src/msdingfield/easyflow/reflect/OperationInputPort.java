package msdingfield.easyflow.reflect;

public interface OperationInputPort extends OperationPort {
	public void set(final Object instance, final Object value) throws IllegalArgumentException, IllegalAccessException;
	public boolean fork();
}
