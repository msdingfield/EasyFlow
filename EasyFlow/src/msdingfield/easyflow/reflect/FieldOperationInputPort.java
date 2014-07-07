package msdingfield.easyflow.reflect;

import java.lang.reflect.Field;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.ForkOn;

/**
 * OperationInputPort implementation for java.lang.reflect.Field properties.
 * 
 * @author Matt
 *
 */
public class FieldOperationInputPort extends FieldOperationPort implements OperationInputPort {
	
	/** Wrap a Field. */
	public FieldOperationInputPort(final Field field) {
		super(field);
	}
	
	@Override
	public void set(final Object instance, final Object value) throws IllegalArgumentException, IllegalAccessException {
		field.set(instance, value);
	}

	@Override
	public boolean fork() {
		return field.isAnnotationPresent(ForkOn.class);
	}

	@Override
	public String getConnectedEdgeName() {
		final Input input = field.getAnnotation(Input.class);
		if (input == null) {
			return getFieldName();
		} else {
			final String explicitEdgeName = input.connectedEdgeName();
			return explicitEdgeName == null || "".equals(explicitEdgeName) ? getFieldName() : explicitEdgeName;
		}
	}
}
