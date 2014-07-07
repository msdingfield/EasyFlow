package msdingfield.easyflow.reflect;

import java.lang.reflect.Field;

import msdingfield.easyflow.annotations.Output;

/**
 * OperationOutputPort implementation for java.lang.reflect.Field properties.
 * 
 * @author Matt
 *
 */
public class FieldOperationOutputPort extends FieldOperationPort implements OperationOutputPort {

	/** Wrap a Field. */
	public FieldOperationOutputPort(final Field field) {
		super(field);
	}

	@Override
	public Object get(final Object instance) throws IllegalArgumentException, IllegalAccessException {
		return field.get(instance);
	}

	@Override
	public String getConnectedEdgeName() {
		final Output output = field.getAnnotation(Output.class);
		if (output == null) {
			return getFieldName();
		} else {
			final String port = output.connectedEdgeName();
			return port == null || "".equals(port) ? getFieldName() : port;
		}
	}

	@Override
	public String toString() {
		return "FieldOperationOutputPort [field=" + field + ", getName()=" + getConnectedEdgeName();
	}

}
