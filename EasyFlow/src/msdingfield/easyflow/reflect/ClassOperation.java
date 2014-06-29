package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

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

	// TODO: Remove builder!!!
	/*
	public static class Builder {
		private final Class<?> operationClass;
		private Constructor<?> constructor = null;
		private Method operationMethod = null;
		private final Set<OperationInputPort> inputs = Sets.newHashSet();
		private final Set<OperationOutputPort> outputs = Sets.newHashSet();

		public Builder(final Class<?> operationClass) {
			this.operationClass = operationClass;
		}

		public Builder setOperationMethod(final Method operationMethod) {
			this.operationMethod = operationMethod;
			return this;
		}

		public Builder setConstructor(final Constructor<?> constructor) {
			this.constructor = constructor;
			return this;
		}

		public Builder addInput(final Field field) {
			inputs.add(new FieldOperationInputPort(field));
			return this;
		}

		public Builder addOutput(final Field field) {
			outputs.add(new FieldOperationOutputPort(field));
			return this;
		}

		public ClassOperation newOperation() {
			final ClassOperation op = new ClassOperation(this);
			return op;
		}
	}*/

}