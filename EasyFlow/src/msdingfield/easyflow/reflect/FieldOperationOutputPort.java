package msdingfield.easyflow.reflect;

import java.lang.reflect.Field;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.ForkOn;

public class FieldOperationOutputPort extends FieldOperationPort implements OperationOutputPort {
	
	public FieldOperationOutputPort(final Field field) {
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
	public String getName() {
		final Input input = field.getAnnotation(Input.class);
		final String port = input.port();
		return port == null || "".equals(port) ? getFieldName() : port;
	}
}
