package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.Vector;

import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

import com.google.common.collect.Sets;

/** Defines an operation which transforms inputs to outputs.
 * 
 * A ClassOperation can only be invoked in the context of a Task.
 * 
 * Instances of ClassOperation are immutable.  All state is held in a Context
 * object provided by the invoker.  When invoked, one or more instances of the
 * class are created and the inputs populated from the context.  When complete
 * the outputs are written back into the context.
 * 
 * A very important implementation consideration is that the operation need not
 * execute synchronously.  It may fork any number of child tasks.  This class
 * must wait until all such child tasks have completed before copying outputs
 * back into the context.
 * 
 * NOTE: The dependency on Task is more than a little awkward.  This class and
 * the Task class could stand to be refactored.  It might be more clear if we
 * had a subclass of Task specific to executing a ClassOperation.
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
	
	public Set<OperationInputPort> getInputs() {
		return operation.getInputs();
	}

	public Set<OperationOutputPort> getOutputs() {
		return operation.getOutputs();
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
	public String toString() {
		return "ClassOperation [operationClass=" + operation.getOperationClass()
				+ ", constructor=" + operation.getConstructor() + ", operationMethod="
				+ operation.getOperationMethod() + ", inputs=" + operation.getInputs() + ", outputs="
				+ operation.getOutputs() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((operation.getConstructor() == null) ? 0 : operation.getConstructor().hashCode());
		result = prime * result + ((operation.getInputs() == null) ? 0 : operation.getInputs().hashCode());
		result = prime * result
				+ ((operation.getOperationClass() == null) ? 0 : operation.getOperationClass().getCanonicalName().hashCode());
		result = prime * result
				+ ((operation.getOperationMethod() == null) ? 0 : operation.getOperationMethod().hashCode());
		result = prime * result + ((operation.getOutputs() == null) ? 0 : operation.getOutputs().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassOperationProxy other = (ClassOperationProxy) obj;
		if (operation.getConstructor() == null) {
			if (other.operation.getConstructor() != null)
				return false;
		} else if (!operation.getConstructor().equals(other.operation.getConstructor()))
			return false;
		if (operation.getInputs() == null) {
			if (other.operation.getInputs() != null)
				return false;
		} else if (!operation.getInputs().equals(other.operation.getInputs()))
			return false;
		if (operation.getOperationClass() == null) {
			if (other.operation.getOperationClass() != null)
				return false;
		} else if (operation.getOperationClass() != other.operation.getOperationClass())
			return false;
		if (operation.getOperationMethod() == null) {
			if (other.operation.getOperationMethod() != null)
				return false;
		} else if (!operation.getOperationMethod().equals(other.operation.getOperationMethod()))
			return false;
		if (operation.getOutputs() == null) {
			if (other.operation.getOutputs() != null)
				return false;
		} else if (!operation.getOutputs().equals(other.operation.getOutputs()))
			return false;
		return true;
	}

	/** Subclass Vector as marker for values aggregated together by the framework. */
	@SuppressWarnings("serial")
	static class Aggregation<T> extends Vector<T> {}

}
