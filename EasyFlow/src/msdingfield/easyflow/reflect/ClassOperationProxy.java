package msdingfield.easyflow.reflect;

import java.lang.reflect.Modifier;

import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

/**
 * Instantiates a ClassOperationInstance and proxies calls to it.
 * 
 * Instances of ClassOperationProxy are immutable.  All state is held in a
 * Context object provided by the caller.
 * 
 * @author Matt
 *
 */
public class ClassOperationProxy {

	public final ClassOperation operation;

	public ClassOperationProxy(final ClassOperation operation) {
		this.operation = operation;
		validate();
	}

	/**
	 * This runs before the operation to create instances and inject inputs.
	 * 
	 * @param context
	 */
	public void before(final Context context) {
		createDelegate(context).before();
	}

	/**
	 * Invoke operation method asynchronously.
	 * 
	 * @param context
	 */
	public void execute(final Context context) {
		getDelegate(context).execute();
	}

	/**
	 * This runs after all operations are complete to copy outputs back into
	 * context.
	 * 
	 * @param context
	 */
	public void after(final Context context) {
		getDelegate(context).after();
	}

	private ClassOperationInstance createDelegate(final Context context) {
		return new ClassOperationInstance(operation, context);
	}

	private ClassOperationInstance getDelegate(final Context context) {
		return (ClassOperationInstance) context.getStateValue(operation.getOperationClass());
	}

	private void validate() {
		if (operation.getOperationClass() == null) {
			throw new InvalidOperationBindingException("operationClass cannot be null.");
		}

		if (Modifier.isAbstract(operation.getOperationClass().getModifiers())) {
			throw new InvalidOperationBindingException("operationClass cannot be abstract.");
		}

		if (operation.getConstructor() == null) {
			throw new InvalidOperationBindingException("constructor cannot be null.");
		}

		if (operation.getConstructor().getParameterTypes().length > 0) {
			throw new InvalidOperationBindingException("constructor must be default (no arg) constructor.");
		}

		if (!Modifier.isPublic(operation.getConstructor().getModifiers())) {
			throw new InvalidOperationBindingException("constructor must be public.");
		}

		if (operation.getOperationMethod() == null) {
			throw new InvalidOperationBindingException("operationMethod cannot be null.");
		}

		if (operation.getOperationMethod().getParameterTypes().length > 0) {
			throw new InvalidOperationBindingException("operationMethod must take an empty parameter list.");
		}

		if (!Modifier.isPublic(operation.getOperationMethod().getModifiers())) {
			throw new InvalidOperationBindingException("operationMethod must be public.");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((operation == null) ? 0 : operation.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ClassOperationProxy other = (ClassOperationProxy) obj;
		if (operation == null) {
			if (other.operation != null) {
				return false;
			}
		} else if (!operation.equals(other.operation)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ClassOperationProxy [" + operation + "]";
	}

}
