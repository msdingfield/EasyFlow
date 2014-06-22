package msdingfield.easyflow.reflect;

public interface OperationOutputPort extends OperationPort {
	public void set(final Object instance, final Object value) throws IllegalArgumentException, IllegalAccessException;
	public boolean fork();
}
