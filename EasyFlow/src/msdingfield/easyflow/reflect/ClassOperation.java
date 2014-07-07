package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Defines an operation which transforms inputs to outputs.
 * 
 * Normally this is used by AnnotationClassOperationBuilder while creating
 * a ClassOperationProxy.
 * 
 * @author Matt
 *
 */
public class ClassOperation {
	/** Class containing the operation method and input/output. */
	private Class<?> operationClass;

	/** The constructor to use when creating instances of operationClass. */
	private Constructor<?> constructor;

	/** The instance method to invoke to transform input to output. */
	private Method operationMethod;

	/** The inputs to the operation. */
	private Set<OperationInputPort> inputs = Sets.newHashSet();

	/** The outputs from the operation. */
	private Set<OperationOutputPort> outputs = Sets.newHashSet();

	public ClassOperation() {

	}

	public ClassOperation(final ClassOperation other) {
		operationClass = other.operationClass;
		constructor = other.constructor;
		operationMethod = other.operationMethod;
		inputs = Sets.newHashSet(other.inputs);
		outputs = Sets.newHashSet(other.outputs);
	}

	public Class<?> getOperationClass() {
		return operationClass;
	}

	public void setOperationClass(final Class<?> operationClass) {
		this.operationClass = operationClass;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public void setConstructor(final Constructor<?> constructor) {
		this.constructor = constructor;
	}

	public Method getOperationMethod() {
		return operationMethod;
	}

	public void setOperationMethod(final Method operationMethod) {
		this.operationMethod = operationMethod;
	}

	public Set<OperationInputPort> getInputs() {
		return inputs;
	}

	public void setInputs(final Set<OperationInputPort> inputs) {
		this.inputs = inputs;
	}

	public void addInput(final OperationInputPort input) {
		this.inputs.add(input);
	}

	public void addInput(final Field input) {
		addInput(new FieldOperationInputPort(input));
	}

	public Set<OperationOutputPort> getOutputs() {
		return outputs;
	}

	public void setOutputs(final Set<OperationOutputPort> outputs) {
		this.outputs = outputs;
	}

	public void addOutput(final OperationOutputPort output) {
		this.outputs.add(output);
	}

	public void addOutput(final Field output) {
		addOutput(new FieldOperationOutputPort(output));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result
				+ ((operationClass == null) ? 0 : operationClass.hashCode());
		result = prime * result
				+ ((operationMethod == null) ? 0 : operationMethod.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
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
		final ClassOperation other = (ClassOperation) obj;
		if (inputs == null) {
			if (other.inputs != null) {
				return false;
			}
		} else if (!inputs.equals(other.inputs)) {
			return false;
		}
		if (operationClass != other.operationClass) {
			return false;
		}
		if (operationMethod == null) {
			if (other.operationMethod != null) {
				return false;
			}
		} else if (!operationMethod.equals(other.operationMethod)) {
			return false;
		}
		if (outputs == null) {
			if (other.outputs != null) {
				return false;
			}
		} else if (!outputs.equals(other.outputs)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ClassOperation [operationClass=" + operationClass
				+ ", operationMethod=" + operationMethod + ", inputs=" + inputs
				+ ", outputs=" + outputs + "]";
	}
}